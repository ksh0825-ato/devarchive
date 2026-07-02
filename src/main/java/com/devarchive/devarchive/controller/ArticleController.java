package com.devarchive.devarchive.controller;

import java.security.Principal;
import java.util.List;
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

import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.Tag;
import com.devarchive.devarchive.dto.article.ArticleDto;
import com.devarchive.devarchive.repository.ArticleRepository;
import com.devarchive.devarchive.service.ArticleService;
import com.devarchive.devarchive.service.JobPostService;

import lombok.RequiredArgsConstructor;
@Controller
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;
    private final JobPostService jobPostService;
    private final ArticleRepository articleRepository;

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
        
        // 1. 로그인한 유저의 username으로 글 조회
        String username = principal.getName();
        Page<Article> myArticles = articleService.findArticlesByUsername(username, pageable);
        
        model.addAttribute("articleCount", articleService.getArticleCount(username));
        model.addAttribute("jobCount", articleService.getUniqueJobCount(username));


        // 2. 모델에 담기
        model.addAttribute("articlePage", myArticles);
        model.addAttribute("username", username);

        return "article/ArticleList";
    }   

    @PostMapping("/ArticleList")
        public String searchMyArticles(Principal principal, Model model,
                                        @RequestParam(value = "keyword", required = false) String keyword,
                                        @PageableDefault(size = 10) Pageable pageable) {
            
            String username = principal.getName();
            
            // 1. 통계 데이터 모델에 추가 (이 부분이 추가되었습니다)
            
            long count = articleService.getArticleCount(username);
            long jobCount = articleService.getUniqueJobCount(username);
    
            // 로그 찍어보기 (콘솔창 확인)
            System.out.println("로그 확인 -> 학습기록 수: " + count + ", 공고 수: " + jobCount);

            model.addAttribute("articleCount", articleService.getArticleCount(username));
            model.addAttribute("jobCount", articleService.getUniqueJobCount(username));
            
            // 검색어가 있으면 검색 결과 조회, 없으면 전체 조회
            Page<Article> articlePage;
            if (keyword != null && !keyword.isEmpty()) {
                articlePage = articleService.searchArticles(username, keyword, pageable);
            } else {
                articlePage = articleService.findArticlesByUsername(username, pageable);
            }
            
            model.addAttribute("articlePage", articlePage);
            model.addAttribute("username", username);
            model.addAttribute("keyword", keyword); // 검색어를 유지하기 위해 모델에 추가
            
            return "article/ArticleList";
        }

    // Article 상세 조회
    @GetMapping("/ArticleDetail/{articleId}")
    public String articleDetail(@PathVariable("articleId") Long articleId, Model model) {
        Article article = articleService.findById(articleId); 
        List<Tag> tags = articleService.getTagsByArticle(article);

        model.addAttribute("article", article);
        model.addAttribute("tags", tags); 
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

    // 공부글 삭제 처리
    @GetMapping("/ArticleDelete")
    public String delete(@RequestParam("articleId") Long articleId) {
        articleService.deleteArticle(articleId); // 위에서 만든 서비스 메서드 호출
        return "redirect:/article/ArticleList";
    }

    @GetMapping("/ArticleSearchByTag")
    public String searchByTag(@RequestParam("tagName") String tagName, Model model) {
        List<Article> articles = articleService.getArticlesByTag(tagName);
        System.out.println("검색된 글 개수: " + (articles != null ? articles.size() : "null"));

      // List를 Page 객체로 변환 (페이징 정보 없이 전체 리스트를 content로 설정)
      Page<Article> articlePage = new PageImpl<>(articles, PageRequest.of(0, 10), articles.size());

        model.addAttribute("articlePage", articlePage); // HTML에서 기대하는 이름으로 전달
        model.addAttribute("tagName", tagName);
        
        // 목록 페이지(ArticleList.html)를 재사용하거나 별도 뷰를 지정
        return "article/ArticleList"; 
    }
    
}