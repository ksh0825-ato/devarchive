package com.devarchive.devarchive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.devarchive.devarchive.dto.account.AccountDto;
import com.devarchive.devarchive.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor

// 3. Controller (화면 요청 처리)
// 사용자가 데이터를 입력하면 위에서 만든 Service를 호출
public class AccountController {
    private final AccountService accountService;

    // 회원가입 화면
    @GetMapping("/account/register")
    public String registerForm() { return "account/register"; }

    // 회원가입 처리
    @PostMapping("/account/register")
    public String register(@Valid AccountDto accountDto, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return alert(model, msg, "back");
        }

        accountService.register(accountDto);
        return "redirect:/account/login";
    }

    // 회원가입 정보 저장 로직
    @PostMapping("/dto/account/save")
    public String save(@Valid AccountDto accountDto, BindingResult bindingResult, Model model) {
        
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return alert(model, msg, "back");
        }

        // redirect: 특정 경로로 브라우저 재요청을 수행. 새로고침 문제를 방지
        return "redirect:/account/login";
    }


    // 로그인 화면 (SecurityConfig에서 지정한 주소와 일치해야 함)
    @GetMapping("/account/login")
        public String loginForm() { 
            return "account/login"; 
        }

    private String alert(Model model, String message, String redirectUrl) {
        model.addAttribute("message", message);
        model.addAttribute("redirectUrl", redirectUrl);
        return "common/alert";
    }
}
    