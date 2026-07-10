package com.devarchive.devarchive.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.dto.account.AccountDto;
import com.devarchive.devarchive.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder; // 비밀번호 비교용

    // 1. 회원가입
    @Transactional // DB 저장 안 되는 오류 픽스 위해 추가
    public void register(AccountDto accountDto) {
        // 1. DTO -> Entity 변환
        Account account = new Account();
        account.setUsername(accountDto.getUsername());
        account.setPassword(passwordEncoder.encode(accountDto.getPassword())); // 중요: 암호화!
        account.setEmail(accountDto.getEmail());
        account.setNickname(accountDto.getNickname());

        // 2. 폼에서 선택한 역할(role)을 엔티티에 직접 설정
        // accountDto에 설정된 값을 account 엔티티로 넘겨줌
        account.setRole(accountDto.getRole());

        accountRepository.save(account);
    }
    

    // 2. 로그인
    public Account login(String username, String password) {
        // 1. 아이디로 회원 조회
        Account account = accountRepository.findByUsername(username)
                .orElse(null); // 없으면 null 반환

        // 2. 조회된 회원이 있고, 비밀번호가 일치하는지 확인
        if (account != null && passwordEncoder.matches(password, account.getPassword())) {
            return account; // 로그인 성공
        }
        return null; // 로그인 실패
    }


    // 3. 다른 서비스(ArticleService 등)에서 특정 유저의 ID를 가져올 때 아주 편리하게 사용됨
    public Long getUserId(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
        return account.getUserId();
    }
}
