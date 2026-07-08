package com.devarchive.devarchive.controller;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.devarchive.devarchive.domain.Account;
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
        
        System.out.println(">>> 컨트롤러 진입 성공! 유저 정보: " + userDetails);

        // 1. Account 객체 조회
        Optional<Account> accountOpt = accountRepository.findByUsername(userDetails.getUsername());
        
        // 만약 로그인은 되었는데 DB에 계정이 없으면 여기서 에러
        if (accountOpt.isEmpty()) {
            return "redirect:/account/login?error=true"; 
        }
        
        Account account = accountOpt.get();
        model.addAttribute("account", account);
        
        // 2. 역할 확인 (null 방어)
        String role = account.getRole();
        boolean isCompany = role != null && role.contains("COMPANY");
        model.addAttribute("isCompany", isCompany);

        // 3. 데이터 조회 (Null 방어)
        model.addAttribute("articleList", !isCompany ? articleService.findAllByUsername(account.getUsername()) : Collections.emptyList());
        model.addAttribute("jobList", jobPostService.findAll());

        return "login/main"; 
    }
}