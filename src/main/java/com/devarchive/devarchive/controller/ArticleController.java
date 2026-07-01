package com.devarchive.devarchive.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.dto.article.ArticleDto;
import com.devarchive.devarchive.service.ArticleService;
import com.devarchive.devarchive.service.JobPostService;

import lombok.RequiredArgsConstructor;
@Controller
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;
    private final JobPostService jobPostService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/ArticleRegister")
    public String ArticleRegisterForm(Model model) {
        // 모든 공고를 가져와 유저가 선택하게 함
        model.addAttribute("jobPosts", jobPostService.getAllJobPosts());
        model.addAttribute("articleDto", new ArticleDto());
        return "article/ArticleRegister";
    }

    @PostMapping("/ArticleRegister")
    public String registerArticle(@ModelAttribute ArticleDto articleDto,
                                 Principal principal,
                                 @RequestParam(value = "jobId", required = false) Long jobId) { // 파라미터 추가
        try {
                // 기존 저장 로직
                String username = principal.getName();
                articleService.saveArticle(articleDto, username, jobId);
                return "redirect:/article/ArticleList";
            } catch (Exception e) {
                // 여기가 핵심입니다. 어떤 에러인지 콘솔에 직접 출력!
                e.printStackTrace(); 
                return "error"; // 에러 페이지로 이동
        }
    }

    @GetMapping("/ArticleList")
        public String myArticleList(Principal principal, Model model, 
                                @PageableDefault(size = 10) Pageable pageable) {
        
        // 1. 로그인한 유저의 username으로 글 조회
        String username = principal.getName();
        Page<Article> myArticles = articleService.findArticlesByUsername(username, pageable);
        
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

}