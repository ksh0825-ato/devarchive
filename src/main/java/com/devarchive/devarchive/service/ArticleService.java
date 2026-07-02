package com.devarchive.devarchive.service;

import com.devarchive.devarchive.repository.StudyProgressRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.ArticleTag;
import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.domain.Tag;
import com.devarchive.devarchive.domain.StudyProgress.ProgressStatus;
import com.devarchive.devarchive.dto.article.ArticleDto;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.repository.ArticleRepository;
import com.devarchive.devarchive.repository.ArticleTagRepository;
import com.devarchive.devarchive.repository.JobPostRepository;
import com.devarchive.devarchive.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final StudyProgressRepository studyProgressRepository;
    private final ArticleRepository articleRepository;
    private final AccountRepository accountRepository;
    private final JobPostRepository jobPostRepository;
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;

    public Page<Article> findArticlesByUsername(String username, Pageable pageable) {
        return articleRepository.findByAccount_Username(username, pageable);
    }

    public Page<Article> searchArticles(String username, String keyword, Pageable pageable) {
    // 제목(title)에 키워드가 포함된 글을 찾는 메서드를 Repository에 추가해야 합니다.
    return articleRepository.findByAccount_UsernameAndTitleContaining(username, keyword, pageable);
    }
    

    @Transactional
    public Article saveArticle(ArticleDto articleDto, String username, Long jobId) {
        // 1. username으로 Account 조회
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 2. Article 엔티티 생성 및 정보 세팅
        Article article = articleDto.toEntity();
        article.setAccount(account); // 유저 정보 연결 (가장 중요!)
        
        // 3. jobId가 있다면 연결 (JobPost 조회 필요)
        if (jobId != null) {
            JobPost jobPost = jobPostRepository.findById(jobId).orElse(null);
            article.setJobPost(jobPost);
        }
        
        return articleRepository.save(article);
    }

    // 상세 조회 로직
    @Transactional(readOnly = true)
    public Article findById(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + articleId));
    }

    @Transactional
    public void update(Long articleId, ArticleDto articleDto) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        
        // 이 시점에 article은 영속 상태입니다. 필드 값만 바꿔주면 끝!
        article.setTitle(articleDto.getTitle());
        article.setContent(articleDto.getContent());
        article.setUpdatedAt(LocalDateTime.now());
        // 만약 JobPost도 변경 가능하다면 여기서 setJobPost()도 호출하세요.
    }

    // 태그 업데이트 로직
    @Transactional
    public void updateArticle(Long articleId, ArticleDto articleDto, String tagNames) {
        Article article = articleRepository.findById(articleId).orElseThrow();

        article.setTitle(articleDto.getTitle());
        article.setContent(articleDto.getContent());
        
        // 1. 기존 태그 매핑 삭제
        List<ArticleTag> oldTags = articleTagRepository.findByArticle(article);
        articleTagRepository.deleteAll(oldTags);
        
        // 2. 새 태그 저장 (기존 saveTagsForArticle 로직 재사용)
        saveTagsForArticle(article, tagNames);
    }

    @Transactional
    public void deleteArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 1. 해당 게시글과 연결된 태그 매핑(ARTICLE_TAG) 먼저 삭제
        List<ArticleTag> articleTags = articleTagRepository.findByArticle(article);
        articleTagRepository.deleteAll(articleTags);
        
        // 2. 그 다음 게시글 삭제
        articleRepository.delete(article);
    }


    @Transactional(readOnly = true)
    public long getArticleCount(String username) {
        return articleRepository.countByAccount_Username(username);
    }

    @Transactional(readOnly = true)
    public long getUniqueJobCount(String username) {
        // 유저가 작성한 학습 기록 중, 연결된 공고(JobPost)가 있는 유니크한 공고 개수
        return articleRepository.countDistinctJobByAccount_Username(username);
    }

    // ArticleService.java에 추가할 예시 로직
    @Transactional
    public void saveTagsForArticle(Article article, String tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;
        
        String[] tags = tagNames.split(",");
        for (String name : tags) {
            String cleanName = name.trim();
            if (cleanName.isEmpty()) continue;
            
            // 2. 태그가 없으면 생성, 있으면 가져오기
            Tag tag = tagRepository.findByTagName(cleanName)
                    .orElseGet(() -> tagRepository.save(new Tag(cleanName)));
            
            // 3. 매핑 테이블(ARTICLE_TAG) 저장
            articleTagRepository.save(new ArticleTag(article, tag));
        }
    }

    @Transactional(readOnly = true)
    public List<Article> getArticlesByTag(String tagName) {
        // 1. 특정 태그가 달린 매핑 정보들을 가져옴
        List<ArticleTag> articleTags = articleTagRepository.findByTagTagName(tagName);
        
        // 2. 매핑 정보에서 Article만 추출하여 리스트로 변환
        return articleTags.stream()
                .map(ArticleTag::getArticle)
                .distinct() // 중복 제거
                .collect(Collectors.toList());
    }

    // ArticleService.java
    public long countStudyingProgress() {
        return studyProgressRepository.countByStatus(ProgressStatus.STUDYING);
    }

    @Transactional(readOnly = true)
    public List<Tag> getTagsByArticle(Article article) {
        // 1. 레포지토리에서 데이터 조회
        List<ArticleTag> articleTags = articleTagRepository.findByArticle(article);
        
        // 2. null 체크 및 빈 리스트 처리 (Null 방어)
        if (articleTags == null || articleTags.isEmpty()) {
            return Collections.emptyList(); // null 대신 비어있는 리스트 반환
        }
        
        // 3. Tag 객체 추출
        return articleTags.stream()
                .map(ArticleTag::getTag)
                .collect(Collectors.toList());
    }
}