package com.devarchive.devarchive.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.dto.article.ArticleDto;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.repository.ArticleRepository;
import com.devarchive.devarchive.repository.JobPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final AccountRepository accountRepository;
    private final JobPostRepository jobPostRepository;

    public Page<Article> findArticlesByUsername(String username, Pageable pageable) {
        return articleRepository.findByAccount_Username(username, pageable);
    }

    public Page<Article> searchArticles(String username, String keyword, Pageable pageable) {
    // 제목(title)에 키워드가 포함된 글을 찾는 메서드를 Repository에 추가해야 합니다.
    return articleRepository.findByAccount_UsernameAndTitleContaining(username, keyword, pageable);
    }

    @Transactional
    public void saveArticle(ArticleDto dto, String username, Long jobId) {
        // 1. 유저 조회
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 2. Article 엔티티 생성 (아까 추가한 기본 생성자 사용)
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setAccount(account); // 유저 매핑
        
        // 3. 공고 매핑 (있을 경우)
        if (jobId != null) {
            JobPost jobPost = jobPostRepository.findById(jobId).orElse(null);
            article.setJobPost(jobPost);
        }
        
        // 4. 저장
        articleRepository.save(article);
    }

    
}