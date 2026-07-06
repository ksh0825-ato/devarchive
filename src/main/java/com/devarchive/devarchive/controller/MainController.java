package com.devarchive.devarchive.controller;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.service.ArticleService;
import com.devarchive.devarchive.service.JobPostService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
@Controller
@RequiredArgsConstructor
public class MainController {

    private final AccountRepository accountRepository;
    private final JobPostService jobPostService;
    private final ArticleService articleService;

    @GetMapping("/")
        public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
            // 1. 비로그인 상태일 경우 로그인 페이지로 이동
            if (userDetails == null) {
                return "redirect:/account/login";
            }

            // 2. 로그인된 사용자 정보 조회
            Account account = accountRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

            // 3. 모델에 기본 정보 추가
            model.addAttribute("account", account);
            boolean isCompany = account.getRole() != null && account.getRole().contains("COMPANY");
            model.addAttribute("isCompany", isCompany);

            // 전체 데이터를 가져오도록 수정
                model.addAttribute("jobList", jobPostService.findAll()); 
                model.addAttribute("articleList", articleService.findAll());

            // 5. 뷰 반환 (기존 login/main 위치라면 그대로 유지)
            return "login/main"; 
        }
    }