package com.devarchive.devarchive.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.StudyProgress;
import com.devarchive.devarchive.repository.ArticleRepository;
import com.devarchive.devarchive.repository.StudyProgressRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ArticleRepository articleRepository;
    private final StudyProgressRepository progressRepository;

    @Override
    @Transactional
    public void run(String... args) {
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