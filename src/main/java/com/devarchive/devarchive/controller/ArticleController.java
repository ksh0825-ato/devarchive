package com.devarchive.devarchive.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.StudyProgress;
import com.devarchive.devarchive.domain.StudyProgress.ProgressStatus;
import com.devarchive.devarchive.domain.Tag;
import com.devarchive.devarchive.domain.Visibility;
import com.devarchive.devarchive.dto.article.ArticleDto;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.repository.ArticleRepository;
import com.devarchive.devarchive.repository.StudyProgressRepository;
import com.devarchive.devarchive.service.ArticleService;
import com.devarchive.devarchive.service.JobPostService;
import com.devarchive.devarchive.service.StudyProgressService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@Controller
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;
    private final JobPostService jobPostService;
    private final ArticleRepository articleRepository;
    private final StudyProgressService studyProgressService;
    private final StudyProgressRepository studyProgressRepository;
    private final AccountRepository accountRepository;

    // 1-1. 학습 기록 작성(get)
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/ArticleRegister")
    public String ArticleRegisterForm(@RequestParam(required = false) Long jobId,
                                      Principal principal, HttpServletRequest request, Model model) {
        
        // 0. 기업 회원 접근 차단
        if (request.isUserInRole("ROLE_COMPANY")) {
            model.addAttribute("message", "기업 회원은 학습글을 작성할 수 없습니다.");
            model.addAttribute("redirectUrl", "back"); // 이전 페이지로 이동
            return "common/alert";
        }

        ArticleDto articleDto = new ArticleDto();
        
        // 1. 만약 jobId가 넘어왔다면 DTO에 설정
        if (jobId != null) {
            articleDto.setJobId(jobId);
        }

        // 2. 모든 공고를 가져와 유저가 선택하게 함
        model.addAttribute("articleDto", articleDto);
        model.addAttribute("jobPosts", jobPostService.getAllJobPosts());
        return "article/ArticleRegister";
    }
    
    // 1-2. 학습 기록 작성(post)
    @PostMapping("/ArticleRegister")
    public String register(@Valid @ModelAttribute("articleDto") ArticleDto articleDto,
                        BindingResult bindingResult, Model model,
                        @RequestParam("tagNames") String tagNames, // 폼에서 받아올 태그 문자열
                        @RequestParam(value = "jobId", required = false) Long jobId,
                        Principal principal) {

        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return alert(model, msg, "back");
        }

        String username = principal.getName();
        
        // 1. 게시글 및 공고 연동 저장
        Article savedArticle = articleService.saveArticle(articleDto, username, jobId);
        
        // 2. 태그 저장
        articleService.saveTagsForArticle(savedArticle, tagNames);
        
        return "redirect:/article/ArticleList";
    }


    // 2-1. 학습 기록 리스트(get)
    @GetMapping("/ArticleList")
    public String myArticleList(Principal principal, Model model, 
                                @PageableDefault(size = 10) Pageable pageable) {

        // 0. 로그인 체크
        if (principal == null) return "redirect:/login";
        String username = principal.getName();

        // 1. 서비스 호출 (페이징 데이터 조회)
        Page<Article> articlePage = articleService.findArticlesByUsername(username, pageable);
        
        // 2. 통계 데이터 조회
        List<StudyProgress> progressList = studyProgressService.getProgressByUsername(username);
        long jobLinkedCount = articleRepository.countUniqueJobPostsByUsername(username);
        long jobCount = articleService.getUniqueJobCount(username);
        long articleCount = articleService.getArticleCount(username);
        long studyingCount = progressList.stream()
                .filter(p -> p.getStatus() == ProgressStatus.STUDYING)
                .count();

        // 3. 모델 전달 (변수명 명확화)
        model.addAttribute("articlePage", articlePage);          // 페이징 정보(next, prev 등) 활용용
        model.addAttribute("articleList", articlePage.getContent()); // 실제 리스트 출력용
        model.addAttribute("jobCount", jobCount);
        model.addAttribute("articleCount", articleCount);
        model.addAttribute("studyingCount", studyingCount);
        model.addAttribute("jobLinkedCount", jobLinkedCount);
        model.addAttribute("totalArticles", articlePage.getTotalElements()); 
        model.addAttribute("progressList", progressList);
        model.addAttribute("isSearchMode", false);

        return "article/ArticleList";
    }

    // 2-2. 학습 기록 리스트(post)
    @PostMapping("/ArticleList")
    public String searchMyArticles(@Valid Principal principal, Model model,
                                    BindingResult bindingResult,
                                    @RequestParam(value = "keyword", required = false) String keyword,
                                    @PageableDefault(size = 10) Pageable pageable) {
        
        // 0. 에러 표시
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return alert(model, msg, "back");
        }
        
        String username = principal.getName();
                
        List<StudyProgress> rawList = studyProgressService.getProgressByUsername(username);


        // 1-1. [중복 제거 핵심] Map의 Key를 jobPost.id로 설정하여 중복을 원천 차단
        Map<Long, StudyProgress> uniqueMap = new java.util.LinkedHashMap<>();
        for (StudyProgress p : rawList) {
            if (p.getJobPost() != null) {
                // 이미 맵에 같은 공고 ID가 있으면 건너뛰고, 없으면 넣음
                uniqueMap.putIfAbsent(p.getJobPost().getJobId(), p);
            }
        }

        // 1-2. 중복 제거된 리스트 생성
        List<StudyProgress> progressList = new ArrayList<>(uniqueMap.values());

        long studyingCount = progressList.stream()
                .filter(p -> p.getStatus() == ProgressStatus.STUDYING)
                .count();


        // 2. 검색 및 데이터 조회
        // 검색 기능 보류 중 (필요 시 검색창 UI와 함께 활성화)
        Page<Article> articlePage = articleService.findArticlesByUsername(username, pageable);

        // Page<Article> articlePage;
        // if (keyword != null && !keyword.isEmpty()) {
        //     articlePage = articleService.searchArticles(username, keyword, pageable);
        // } else {
        //     articlePage = articleService.findArticlesByUsername(username, pageable);
        // }
    

        // 3. 모델에 모두 담기
        model.addAttribute("articleCount", articleService.getArticleCount(username));
        model.addAttribute("jobCount", articleService.getUniqueJobCount(username));
        model.addAttribute("studyingCount", studyingCount); // 통계 추가
        model.addAttribute("articlePage", articlePage);
        model.addAttribute("totalArticles", articlePage.getTotalElements()); // 전체 개수 동기화
        model.addAttribute("progressList", progressList);
        model.addAttribute("username", username);
        model.addAttribute("keyword", keyword);
        model.addAttribute("isSearchMode", true);

        return "article/ArticleList";
    }


    // 3. 학습 기록 상세 조회(get)
    @GetMapping("/ArticleDetail/{articleId}")
    public String articleDetail(@PathVariable("articleId") Long articleId, Model model, Principal principal, HttpServletRequest request) {
        Article article = articleService.findById(articleId); 
        List<Tag> tags = articleService.getTagsByArticle(article);

        // 0. 예외 처리를 포함한 Account 조회
        Account account = accountRepository.findByUsername(principal.getName())
                        .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자 정보를 찾을 수 없습니다."));

        // 1. Article이 연관된 JobPost가 있다면 해당 공고에 대한 진행 상황을 조회
        StudyProgress studyProgress = null;
            
        // 2. article이 jobPost를 가지고 있는지 확인 (삭제된 공고인 경우 방지)
            if (article.getJobPost() != null) {
                // 2. Repository 메서드 이름을 수정된 것에 맞게 호출
                studyProgress = studyProgressRepository
                    .findByAccountUsernameAndJobPost(principal.getName(), article.getJobPost())
                    .orElse(null); 
            }

        String role = account.getRole();
        boolean isCompany = role != null && role.contains("COMPANY");    


        model.addAttribute("article", article);
        model.addAttribute("tags", tags);
        model.addAttribute("studyProgress", studyProgress);
        model.addAttribute("isCompany", isCompany);
        return "article/ArticleDetail";
    }


    // 4-1. 학습 기록 수정(get)
    @GetMapping("/ArticleUpdate")
    public String updateForm(@RequestParam("articleId") Long articleId, Model model, HttpServletRequest request, Principal principal) {

        // 0. 기업 회원 접근 차단
        if (request.isUserInRole("ROLE_COMPANY")) {
                model.addAttribute("message", "기업 회원은 학습글을 수정할 수 없습니다.");
                model.addAttribute("redirectUrl", "back"); // 이전 페이지로 이동
                return "common/alert";
        }

        Article article = articleService.findById(articleId);
        List<Tag> tags = articleService.getTagsByArticle(article); 

        model.addAttribute("article", article);
        
        // 1. 태그를 콤마로 구분된 문자열로 변환하여 입력창에 기본값으로 세팅
        String tagNames = tags.stream()
                            .map(Tag::getTagName)
                            .collect(Collectors.joining(", "));
        model.addAttribute("tagNames", tagNames);
        
        return "article/ArticleUpdate";
    }

    // 4-2. 학습 기록 수정(post)
    @PostMapping("/ArticleUpdate")
    public String updateArticle(@Valid @RequestParam("articleId") Long articleId,
                                @ModelAttribute ArticleDto articleDto,
                                @RequestParam(value = "tagNames", required = false) String tagNames,
                                BindingResult bindingResult, Model model) {
        // 0. 에러 처리
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return alert(model, msg, "back");
        }

        articleService.updateArticle(articleId, articleDto, tagNames);
        
        return "redirect:/article/ArticleDetail/" + articleId;
    }


    // 5. 학습 기록 삭제
    @GetMapping("/ArticleDelete")
    public String delete(@RequestParam("articleId") Long articleId, Principal principal, HttpServletRequest request, Model model) {
        
        // 1. 로그인 여부 확인
        if (principal == null) {
            return "redirect:/";
        }

        // 2. principal.getName()으로 실제 username을 가져와 전달
        articleService.deleteArticle(articleId, principal.getName());
        
        return "redirect:/article/ArticleList";
    }

    // 6. 학습 기록 태그 조회
    @GetMapping("/ArticleSearchByTag")
    public String searchByTag(@RequestParam("tagName") String tagName, Principal principal, Model model, HttpServletRequest request) {
        String username = principal.getName();

        // 0. 기업 회원 접근 차단
            if (request.isUserInRole("ROLE_COMPANY")) {
                model.addAttribute("message", "기업 회원은 학습글을 태그 검색할 수 없습니다.");
                model.addAttribute("redirectUrl", "back");
                return "common/alert";
            }

        List<Article> articles = articleService.getArticlesByTag(tagName);
        Page<Article> articlePage = new PageImpl<>(articles, PageRequest.of(0, 10), (long) articles.size());

        long jobLinkedCount = articleRepository.countUniqueJobPostsByUsername(username);

        // 통계 데이터 추가 (서비스에 아래 메서드들이 구현되어 있어야 함)
        List<StudyProgress> progressList = studyProgressService.getProgressByUsername(username);
            long studyingCount = progressList.stream()
                    .filter(p -> p.getStatus() == ProgressStatus.STUDYING)
                    .count();

        model.addAttribute("articlePage", articlePage);
        model.addAttribute("tagName", tagName);
        model.addAttribute("isSearchMode", true); // 검색 결과 모드임을 알리는 플래그 
        model.addAttribute("progressList", progressList);
        model.addAttribute("jobLinkedCount", jobLinkedCount);
        model.addAttribute("totalArticles", (long) articles.size()); // 리스트 사이즈를 명확히 할당
        model.addAttribute("studyingCount", studyingCount);

        return "article/ArticleList"; 
    }


    // 7. 공개 학습 기록 리스트(get)
    @GetMapping("/ArticlePublicList")
    public String publicArticleList(Model model, @PageableDefault(size = 10) Pageable pageable) {
        List<Article> publicArticles = articleRepository.findByVisibilityOrderByCreatedAtDesc(Visibility.PUBLIC);

        Page<Article> articlePage = articleService.findAllPublicArticles(pageable); // Page 타입으로 조회 필수

        model.addAttribute("articlePage", articlePage);
        model.addAttribute("articles", publicArticles);
        model.addAttribute("articles", articlePage.getContent());
        return "article/ArticlePublicList"; // 뷰 파일 경로
    }

    
    // alert용
    private String alert(Model model, String message, String redirectUrl) {
        model.addAttribute("message", message);
        model.addAttribute("redirectUrl", redirectUrl);
        return "common/alert";
    }

}