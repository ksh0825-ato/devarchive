package com.devarchive.devarchive.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.StudyProgress;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.repository.ArticleRepository;
import com.devarchive.devarchive.repository.StudyProgressRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    private final ArticleRepository articleRepository;
    private final StudyProgressRepository progressRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (accountRepository.findByUsername("user1").isEmpty()) {
            Account account = new Account();
            account.setUsername("user1");
            account.setPassword(passwordEncoder.encode("1234")); // 여기서 현재 인코더로 암호화!
            account.setNickname("테스터1");
            account.setRole("ROLE_USER");
            accountRepository.save(account);
        }

        // 1. 데이터 보정 로직
        List<Article> articles = articleRepository.findAll();
        
        for (Article article : articles) {
            // jobPost가 있는 경우에만 진행 상태 체크
            if (article.getJobPost() != null) {
                boolean exists = progressRepository.existsByAccountAndJobPost(
                        article.getAccount(), article.getJobPost());
                
                // 데이터가 없으면 '진행 중'으로 자동 생성
                if (!exists) {
                    StudyProgress progress = new StudyProgress();
                    progress.setAccount(article.getAccount());
                    progress.setJobPost(article.getJobPost());
                    progress.setStatus(StudyProgress.ProgressStatus.STUDYING);
                    progressRepository.save(progress);
                }
            }
        }
    }
}