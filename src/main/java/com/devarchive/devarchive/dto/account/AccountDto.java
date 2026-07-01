package com.devarchive.devarchive.dto.account;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class AccountDto {
    private Long userId;
    private String username;
    private String password;
    private String email;
    private String nickname;
    
    private String role;
    private LocalDateTime createdAt = LocalDateTime.now();
}
