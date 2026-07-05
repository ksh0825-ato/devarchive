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
import com.devarchive.devarchive.dto.article.ArticleDto;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.repository.ArticleRepository;
import com.devarchive.devarchive.repository.StudyProgressRepository;
import com.devarchive.devarchive.service.ArticleService;
import com.devarchive.devarchive.service.JobPostService;
import com.devarchive.devarchive.service.StudyProgressService;

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

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/ArticleRegister")
    public String ArticleRegisterForm(Model model) {
        // 모든 공고를 가져와 유저가 선택하게 함
        model.addAttribute("jobPosts", jobPostService.getAllJobPosts());
        model.addAttribute("articleDto", new ArticleDto());
        return "article/ArticleRegister";
    }

    @PostMapping("/ArticleRegister")
    public String register(@ModelAttribute ArticleDto articleDto, 
                        @RequestParam("tagNames") String tagNames, // 폼에서 받아올 태그 문자열
                        @RequestParam(value = "jobId", required = false) Long jobId,
                        Principal principal) {
        
        String username = principal.getName();
        
        // 1. 게시글 및 공고 연동 저장
        Article savedArticle = articleService.saveArticle(articleDto, username, jobId);
        
        // 2. 태그 저장
        articleService.saveTagsForArticle(savedArticle, tagNames);
        
        return "redirect:/article/ArticleList";
    }

    @GetMapping("/ArticleList")
    public String myArticleList(Principal principal, Model model, 
                                @PageableDefault(size = 10) Pageable pageable) {
        
        String username = principal.getName();
        Page<Article> myArticles = articleService.findArticlesByUsername(username, pageable);

        // 1. 여기서 서비스의 정확한 카운트 메서드를 호출합니다.
        long jobCount = articleService.getUniqueJobCount(username);

        // 1. 로그인한 유저의 진행 상황 리스트만 가져오기 (서비스에 이 메서드 구현 필요)
        List<StudyProgress> progressList = studyProgressService.getProgressByUsername(username);
        
        // 2. 진행 중(STUDYING)인 개수만 별도 계산
        long studyingCount = progressList.stream()
                .filter(p -> p.getStatus() == ProgressStatus.STUDYING)
                .count();

        // 2. 모델에 정확한 이름으로 담습니다.
        model.addAttribute("jobCount", jobCount);

        model.addAttribute("articleCount", articleService.getArticleCount(username));
        model.addAttribute("jobCount", articleService.getUniqueJobCount(username));
        model.addAttribute("studyingCount", studyingCount); // 이 값을 추가해야 합니다!

        model.addAttribute("articlePage", myArticles);
        model.addAttribute("totalArticles", myArticles.getTotalElements()); 
        model.addAttribute("progressList", progressList);
        model.addAttribute("isSearchMode", false);

        return "article/ArticleList";
    }

    @PostMapping("/ArticleList")
    public String searchMyArticles(Principal principal, Model model,
                                    @RequestParam(value = "keyword", required = false) String keyword,
                                    @PageableDefault(size = 10) Pageable pageable) {
        
        String username = principal.getName();
                
        List<StudyProgress> rawList = studyProgressService.getProgressByUsername(username);

        // [중복 제거 핵심] Map의 Key를 jobPost.id로 설정하여 중복을 원천 차단
        Map<Long, StudyProgress> uniqueMap = new java.util.LinkedHashMap<>();
        for (StudyProgress p : rawList) {
            if (p.getJobPost() != null) {
                // 이미 맵에 같은 공고 ID가 있으면 건너뛰고, 없으면 넣음
                uniqueMap.putIfAbsent(p.getJobPost().getJobId(), p);
            }
        }

        // 중복 제거된 리스트 생성
        List<StudyProgress> progressList = new ArrayList<>(uniqueMap.values());


        // [중간 확인] 로그 찍기
        System.out.println(">>> 모델에 담기 직전 리스트 사이즈: " + progressList.size());
        for (StudyProgress p : progressList) {
            System.out.println(">>> 회사 이름: " + p.getJobPost().getCompanyName());
        }

        System.out.println(">>> 조회된 progressList 크기: " + progressList.size());
        progressList.forEach(p -> System.out.println(">>> 상태 확인: " + p.getStatus()));
        
        long studyingCount = progressList.stream()
                .filter(p -> p.getStatus() == ProgressStatus.STUDYING)
                .count();

        System.out.println(">>> 필터링 후 studyingCount: " + studyingCount);
        
        // 2. 검색 및 데이터 조회
        Page<Article> articlePage;
        if (keyword != null && !keyword.isEmpty()) {
            articlePage = articleService.searchArticles(username, keyword, pageable);
        } else {
            articlePage = articleService.findArticlesByUsername(username, pageable);
        }
        
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

    // Article 상세 조회
    @GetMapping("/ArticleDetail/{articleId}")
    public String articleDetail(@PathVariable("articleId") Long articleId, Model model, Principal principal) {
        Article article = articleService.findById(articleId); 
        List<Tag> tags = articleService.getTagsByArticle(article);
        
        // 예외 처리를 포함한 Account 조회
        Account account = accountRepository.findByUsername(principal.getName())
                        .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자 정보를 찾을 수 없습니다."));

        // Article이 연관된 JobPost가 있다면 해당 공고에 대한 진행 상황을 조회
        StudyProgress studyProgress = null;
            
        // 1. article이 jobPost를 가지고 있는지 확인 (삭제된 공고인 경우 방지)
            if (article.getJobPost() != null) {
                // 2. Repository 메서드 이름을 수정된 것에 맞게 호출
                studyProgress = studyProgressRepository
                    .findByAccountUsernameAndJobPost(principal.getName(), article.getJobPost())
                    .orElse(null); 
            }

        model.addAttribute("article", article);
        model.addAttribute("tags", tags);
        model.addAttribute("studyProgress", studyProgress); // 모델에 추가
        return "article/ArticleDetail";
    }

    @GetMapping("/ArticleUpdate")
    public String updateForm(@RequestParam("articleId") Long articleId, Model model) {
        System.out.println("조회할 articleId: " + articleId); // 로그 추가
        
        Article article = articleService.findById(articleId);
        
        if (article == null) {
            System.out.println("해당 ID의 게시글이 없습니다!");
            return "error/404"; // 혹은 적절한 에러 페이지
        }
    
        List<Tag> tags = articleService.getTagsByArticle(article);
        
        model.addAttribute("article", article);
        
        // 태그를 콤마로 구분된 문자열로 변환하여 입력창에 기본값으로 세팅
        String tagNames = tags.stream()
                            .map(Tag::getTagName)
                            .collect(Collectors.joining(", "));
        model.addAttribute("tagNames", tagNames);
        
        return "article/ArticleUpdate";
    }

    // 2. 실제 데이터 업데이트 (POST)
    @PostMapping("/ArticleUpdate")
    public String updateArticle(@RequestParam("articleId") Long articleId, 
                                @ModelAttribute ArticleDto articleDto,
                                @RequestParam(value = "tagNames", required = false) String tagNames) {
        articleService.updateArticle(articleId, articleDto, tagNames);
        return "redirect:/article/ArticleDetail/" + articleId;
    }

    @GetMapping("/ArticleDelete")
    public String delete(@RequestParam("articleId") Long articleId, Principal principal) {
        // 1. 로그인 여부 확인
        if (principal == null) {
            return "redirect:/";
        }

        // 2. principal.getName()으로 실제 username을 가져와 전달
        articleService.deleteArticle(articleId, principal.getName());
        
        return "redirect:/article/ArticleList";
    }

    @GetMapping("/ArticleSearchByTag")
    public String searchByTag(@RequestParam("tagName") String tagName, Principal principal, Model model) {
        String username = principal.getName();
        
        List<Article> articles = articleService.getArticlesByTag(tagName);
        // Page 객체 생성 시 정확한 전체 사이즈를 넘겨줌
        Page<Article> articlePage = new PageImpl<>(articles, PageRequest.of(0, 10), (long) articles.size());
        System.out.println("검색된 글 개수: " + (articles != null ? articles.size() : "null"));

        // 1. 통계 데이터 추가 (서비스에 아래 메서드들이 구현되어 있어야 함)
        List<StudyProgress> progressList = studyProgressService.getProgressByUsername(username);
            long studyingCount = progressList.stream()
                    .filter(p -> p.getStatus() == ProgressStatus.STUDYING)
                    .count();

        // 2. 모델에 전달
        model.addAttribute("articlePage", articlePage);
        model.addAttribute("tagName", tagName);
        model.addAttribute("isSearchMode", true); // 검색 결과 모드임을 알리는 플래그 
        model.addAttribute("progressList", progressList);
        model.addAttribute("totalArticles", (long) articles.size()); // 리스트 사이즈를 명확히 할당
        model.addAttribute("studyingCount", studyingCount);

        // 목록 페이지(ArticleList.html)를 재사용하거나 별도 뷰를 지정
        return "article/ArticleList"; 
    }
    
}