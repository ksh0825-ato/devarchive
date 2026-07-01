package com.devarchive.devarchive.controller;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
@Controller
@RequiredArgsConstructor
public class MainController {

    private final AccountRepository accountRepository;

    @GetMapping("/")
    public String index(Principal principal) {
        // 로그인 상태라면 바로 메인 대시보드로 리다이렉트
        if (principal != null) {
            return "redirect:/login/main";
        }
        // 비로그인 상태라면 로그인 페이지로 리다이렉트
        return "redirect:/account/login";
    }

    @GetMapping("/login/main")
    public String main(Principal principal, Model model) {
        // 인증되지 않은 사용자가 강제로 이 주소로 들어올 경우 방어
        if (principal == null) {
            return "redirect:/account/login";
        }

        Account account = accountRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        model.addAttribute("account", account);
        boolean isCompany = account.getRole() != null && account.getRole().contains("COMPANY");
        model.addAttribute("isCompany", isCompany);

        return "login/main"; 
    }
}