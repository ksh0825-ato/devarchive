package com.devarchive.devarchive.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final AccountRepository accountRepository;

    @ModelAttribute("account")
    public Account getAccount(Principal principal) {

        if (principal == null) return null;
        // 여기에서 Repository를 통해 DB에서 Account를 조회하여 반환
        return accountRepository.findByUsername(principal.getName()).orElse(null);
    }
}