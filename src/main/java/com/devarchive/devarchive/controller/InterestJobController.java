package com.devarchive.devarchive.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.InterestJob;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.service.InterestJobService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class InterestJobController {
    
    private final InterestJobService interestJobService;
    private final AccountRepository accountRepository;

    // 1-1. 관심 목록 조회(get)
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/interest")
    public String interestList(@AuthenticationPrincipal UserDetails userDetails,
                                    Model model, HttpServletRequest request) {

        // 1-1. 로그인 유저 확인
        Account account = accountRepository.findByUsername(userDetails.getUsername())
                           .orElseThrow(() -> new IllegalArgumentException("로그인이 필요합니다."));

        // 1-2. 기업 회원 접근 차단
        if (request.isUserInRole("ROLE_COMPANY")) {
            model.addAttribute("message", "기업 회원은 공고 글을 북마크 할 수 없습니다.");
            model.addAttribute("redirectUrl", "back"); // 이전 페이지로 이동
            return "common/alert";
        }

        // 2. 서비스 호출
        List<InterestJob> interestList = interestJobService.findAllByUserId(account.getUserId());

        // 3. 모델 전달
        model.addAttribute("interestList", interestList);

        return "api/interest";
    }

    // 1-2. 관심 목록 조회(post)
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/interest/{jobId}")
    public String toggleInterest(@PathVariable Long jobId, 
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {

        // 1. 로그인 확인 (이전과 동일)
        Account account = accountRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("로그인이 필요합니다."));

        // 2. 서비스 로직 수행
        interestJobService.toggleInterest(account.getUserId(), jobId);
        
        // 3. 페이지 새로고침 (상세 페이지로 리다이렉트)
        return "redirect:/job/JobPostDetail?jobId=" + jobId;
    }
}