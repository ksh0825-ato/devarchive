package com.devarchive.devarchive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.devarchive.devarchive.dto.account.AccountDto;
import com.devarchive.devarchive.service.AccountService;

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
    public String register(AccountDto accountDto) {
        System.out.println("컨트롤러 호출됨!"); // 디버깅용
        accountService.register(accountDto);
        return "redirect:/account/login";
    }

    // 회원가입 정보 저장 로직?
    @PostMapping("/dto/account/save")
    public String save(AccountDto accountDto) {
        System.out.println(accountDto);
        // post 방식이므로 추후에 DB 연결해서 회원 데이터 등록을 수행

        // redirect: 특정 경로로 브라우저 재요청을 수행. 새로고침 문제를 방지
        return "redirect:/account/login";
    }

    // 로그인 화면 (SecurityConfig에서 지정한 주소와 일치해야 함)
    @GetMapping("/account/login")
    public String loginForm() { return "account/login"; }
    // 여기서 "login"은 templates 폴더의 login.html을 의미
    
    // @PostMapping("/account/login")
    // public String login(@RequestParam String username, 

    //     @RequestParam String password, 
    //     HttpServletRequest request) {
            
    //         Account loginAccount = accountService.login(username, password);
            
    //         if (loginAccount == null) {
    //             return "account/login"; // 실패 시 다시 로그인 페이지
    //         }
            
    //         // 로그인 성공 시 세션 생성
    //         HttpSession session = request.getSession();
    //         session.setAttribute("loginAccount", loginAccount); // 세션에 회원 객체 저장
            
    //         return "redirect:/login/main"; // main(로그인 후 화면)로 이동
    //     }
        
    }
    