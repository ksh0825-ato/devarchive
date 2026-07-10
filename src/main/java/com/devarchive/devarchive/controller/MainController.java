package com.devarchive.devarchive.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.Visibility;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.repository.ArticleRepository;
import com.devarchive.devarchive.service.AccountService;
import com.devarchive.devarchive.service.JobPostService;

import lombok.RequiredArgsConstructor;
@Controller
@RequiredArgsConstructor
public class MainController {

    private final AccountRepository accountRepository;
    private final JobPostService jobPostService;
    private final ArticleRepository articleRepository;
    private final AccountService accountService;

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model, Principal principal) {
        
        if (userDetails == null) {
            return "redirect:/account/login";
        }
        
        // 1. Account 객체 조회
        Optional<Account> accountOpt = accountRepository.findByUsername(userDetails.getUsername());
        
        if (accountOpt.isEmpty()) {
            return "redirect:/account/login?error=true"; 
        }

        Account account = accountOpt.get();
        model.addAttribute("account", account);


        // 2. 역할 확인
        String role = account.getRole();
        boolean isCompany = role != null && role.contains("COMPANY");
        model.addAttribute("isCompany", isCompany);

        
        // 3. 데이터 조회
        // (A) 내 학습 기록 (개인용)
        Long userId = accountService.getUserId(principal.getName());
        List<Article> myArticles = articleRepository.findByAccountUserIdOrderByCreatedAtDesc(userId);
        model.addAttribute("myArticles", myArticles);
        
        // (B) 공유 학습 기록 (공유 게시판용)
        List<Article> publicArticles = articleRepository.findByVisibilityOrderByCreatedAtDesc(Visibility.PUBLIC);
        model.addAttribute("publicArticles", publicArticles);


        // 4. 기타 데이터
        model.addAttribute("jobList", jobPostService.getAllJobPosts());

        return "login/main"; 
    }
}