package com.devarchive.devarchive.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor


// 2. 로그인 및 세션 관리 (Spring Security 활용)
// Spring Security를 사용하면 세션 관리와 로그인 검증을 직접 짤 필요 없이 프레임워크가 대신 해줌
// 우리는 UserDetailsService를 구현하여 Spring Security에게 "어디서 사용자 정보를 가져오는지"만 알려주면 됨

public class CustomUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // Spring Security가 로그인 검증에 사용하는 User 객체 반환
        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                // 수정 예시
                .roles(account.getRole().replace("ROLE_", "")) // ROLE_ 접두사를 제거하고 넘김
                .build();
    }
}    