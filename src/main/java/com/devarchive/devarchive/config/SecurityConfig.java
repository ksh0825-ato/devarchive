package com.devarchive.devarchive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        // .exceptionHandling(exception -> exception
        //     .accessDeniedHandler((request, response, accessDeniedException) -> {
        //         // 403 발생 시 로직
        //         request.setAttribute("message", "권한이 없는 페이지입니다.");
        //         request.setAttribute("redirectUrl", "back");
        //         request.getRequestDispatcher("/common/alert").forward(request, response);
        //     })
        // )
        
        .csrf(csrf -> csrf.disable()) // 일단 테스트를 위해 CSRF 끄기
            .authorizeHttpRequests(auth -> auth
                // 목록 조회나 상세 조회는 로그인한 사람이라면 누구나 볼 수 있게 허용
                // 로그인 페이지와 메인 진입 페이지는 모두 접근 가능해야 함
                    .requestMatchers("/account/register", "/", "/common/alert", "/account/login", "/job/JobPostList", "/job/JobPostDetail", "/login/main", "/css/**", "/js/**").permitAll() 
                    
                    // 기존 설정들
                    .requestMatchers("/job/JobPostRegister", "/job/JobPostUpdate", "/job/JobPostDelete").hasRole("COMPANY")
                    // .requestMatchers("/article/**").hasRole("USER")
                    .anyRequest().authenticated()
            )
        
        .formLogin(form -> form
        .loginPage("/account/login")
        .defaultSuccessUrl("/", true) // 로그인 성공 시 무조건 목록 페이지로 이동
        .permitAll()
        )
    
        .logout(logout -> logout
        .logoutSuccessUrl("/account/login")
    );

    return http.build();
}

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}