package com.devarchive.devarchive.config;

import java.io.IOException;
import java.net.URLEncoder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/account/register", "/", "/api/interst", "/common/alert", "/account/login", "/job/JobPostList", "/job/JobPostDetail", "/account/loginProc", "/login/main", "/css/**", "/js/**").permitAll()
            .requestMatchers("/job/JobPostRegister", "/job/JobPostUpdate", "/job/JobPostDelete").hasRole("COMPANY")
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/account/login") // 로그인 페이지
            .loginProcessingUrl("/account/login") // 폼 전송 주소
            .defaultSuccessUrl("/", true)
            .failureHandler((request, response, exception) -> { // 람다식으로 간결하게!
                String errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
                if (exception instanceof BadCredentialsException) {
                    errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
                } else if (exception instanceof DisabledException) {
                    errorMessage = "계정이 비활성화되었습니다.";
                }
                // 주소를 "/account/login"으로 수정
                response.sendRedirect("/account/login?error=true&exception=" + URLEncoder.encode(errorMessage, "UTF-8"));
            })
        )
        .logout(logout -> logout
            .logoutUrl("/logout") // 로그아웃 요청 주소
            .logoutSuccessUrl("/account/login")
        );

    return http.build();
 }

     @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}