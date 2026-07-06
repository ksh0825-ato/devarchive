package com.devarchive.devarchive.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.service.ArticleService;
import com.devarchive.devarchive.service.JobPostService;

import lombok.RequiredArgsConstructor;
@Controller
@RequiredArgsConstructor
public class MainController {

    private final AccountRepository accountRepository;
    private final JobPostService jobPostService;
    private final ArticleService articleService;

   @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/account/login";
        }

        Account account = accountRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        model.addAttribute("account", account);
        boolean isCompany = account.getRole() != null && account.getRole().contains("COMPANY");
        model.addAttribute("isCompany", isCompany);

        // 1. 기업 사용자가 아닐 때만 학습글 조회
        if (!isCompany) {
                String username = userDetails.getUsername();
                // 페이지네이션 없이 전체를 리스트로 가져오기
                List<Article> myArticles = articleService.findAllByUsername(username);
                model.addAttribute("articleList", myArticles); 
            } else {
                model.addAttribute("articleList", Collections.emptyList());
        }
        
        // 공고는 전체를 보여주어도 무방하다면 그대로 둡니다.
        model.addAttribute("jobList", jobPostService.findAll()); 

        return "login/main"; 
    }
}