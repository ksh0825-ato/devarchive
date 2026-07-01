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
    @GetMapping("/ArticleDetail")
    public String articleDetail(@RequestParam("articleId") Long articleId, Model model) {
        Article article = articleService.findById(articleId); // 혹은 articleService.getArticle(articleId)
        model.addAttribute("article", article);
        return "article/ArticleDetail";
    }

    // 1. 수정 페이지로 이동
    @GetMapping("/ArticleUpdate")
    public String updateForm(@RequestParam("articleId") Long articleId, Model model) {
        Article article = articleService.findById(articleId);
        model.addAttribute("article", article);
        return "article/ArticleUpdate"; // 수정할 폼이 있는 HTML
    }

    // 2. 실제 데이터 업데이트 (POST)
    @PostMapping("/ArticleUpdate")
    public String updateArticle(@RequestParam("articleId") Long articleId, 
                                @ModelAttribute ArticleDto articleDto) {
        articleService.update(articleId, articleDto);
        return "redirect:/article/ArticleDetail?articleId=" + articleId; // 상세 페이지로 복귀
    }


    // 공부글 삭제 처리
    @GetMapping("/ArticleDelete")
      public String delete(@RequestParam("articleId") Long articleId) {
        articleRepository.deleteById(articleId);
        return "redirect:/article/ArticleList"; // 삭제 후 목록으로 이동
    }
    

}