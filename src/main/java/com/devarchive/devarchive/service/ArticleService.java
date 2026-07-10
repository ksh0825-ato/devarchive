package com.devarchive.devarchive.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.ArticleTag;
import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.domain.StudyProgress;
import com.devarchive.devarchive.domain.StudyProgress.ProgressStatus;
import com.devarchive.devarchive.domain.Tag;
import com.devarchive.devarchive.dto.article.ArticleDto;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.repository.ArticleRepository;
import com.devarchive.devarchive.repository.ArticleTagRepository;
import com.devarchive.devarchive.repository.JobPostRepository;
import com.devarchive.devarchive.repository.StudyProgressRepository;
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

    public List<Article> findAllByUsername(String username){
        return articleRepository.findByAccount_Username(username);
    }

    public List<Article> findRecentArticles(String username) {
        // createdAt 기준 내림차순(최신순) 정렬, 0페이지에서 5개 가져오기
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        return articleRepository.findByAccount_Username(username, pageable).getContent();
    }

    public Page<Article> searchArticles(String username, String keyword, Pageable pageable) {
    // 제목(title)에 키워드가 포함된 글을 찾는 메서드를 Repository에 추가해야 합니다.
    return articleRepository.findByAccount_UsernameAndTitleContaining(username, keyword, pageable);
    }
    

    @Transactional
    public Article saveArticle(ArticleDto articleDto, String username, Long jobId) {
        // 1. 사용자 조회
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 2. Article 엔티티 생성
        Article article = articleDto.toEntity();
        article.setAccount(account);
        
        // 3. JobPost 연결 및 StudyProgress 자동 생성 로직
        if (jobId != null) {
            JobPost jobPost = jobPostRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 공고를 찾을 수 없습니다."));
            article.setJobPost(jobPost);
            
            // 해당 공고에 대해 이미 '학습 중'인 기록이 없다면 새로 생성
            if (!studyProgressRepository.existsByAccountAndJobPost(account, jobPost)) {
                StudyProgress progress = StudyProgress.builder()
                        .account(account)
                        .jobPost(jobPost)
                        .status(ProgressStatus.STUDYING)
                        .updatedAt(LocalDateTime.now())
                        .build();
                studyProgressRepository.save(progress);
            }
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
        
        if (articleDto.getVisibility() != null) {
            article.setVisibility(articleDto.getVisibility());
        }
    }

    // 태그 업데이트 로직
    @Transactional
    public void updateArticle(Long articleId, ArticleDto articleDto, String tagNames) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        article.setTitle(articleDto.getTitle());
        article.setContent(articleDto.getContent());
        
        if (articleDto.getVisibility() != null) {
            article.setVisibility(articleDto.getVisibility());
        }

        // 1. 기존 태그 매핑 삭제
        List<ArticleTag> oldTags = articleTagRepository.findByArticle(article);
        articleTagRepository.deleteAll(oldTags);
        
        // 2. 새 태그 저장 (기존 saveTagsForArticle 로직 재사용)
        saveTagsForArticle(article, tagNames);

    }

    public List<Article> findAll() {
        // 2. 클래스 이름(ArticleRepository)이 아니라 
        // 1번에서 선언한 변수명(articleRepository)으로 호출합니다.
        return articleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Article> findAllPublicArticles(Pageable pageable) {
        return articleRepository.findAllPublicArticles(pageable);
    }
    
    @Transactional
    public void deleteArticle(Long articleId, String username) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!article.getAccount().getUsername().equals(username)) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        // 1. 글 삭제 전, 연결된 공고 정보 확보
        JobPost jobPost = article.getJobPost();
        
        if (!articleRepository.existsByAccountAndJobPost(article.getAccount(), jobPost)) {
        studyProgressRepository.deleteByAccountAndJobPost(article.getAccount(), jobPost);
        }
        
        // 2. 글 삭제
        articleRepository.delete(article);
        
        // 3. 만약 연결된 공고가 있고, 해당 유저가 쓴 다른 글이 없다면 StudyProgress 삭제
        if (jobPost != null) {
            boolean existsOtherArticles = articleRepository.existsByAccountAndJobPost(article.getAccount(), jobPost);
            
            if (!existsOtherArticles) {
                studyProgressRepository.deleteByAccountAndJobPost(article.getAccount(), jobPost);
            }
        }
    }


    @Transactional(readOnly = true)
    public long getUniqueJobCount(String username) {
        // 공고가 연결된 게시글들의 job_id를 중복 제거하여 카운트
        return articleRepository.countUniqueJobPostsByUsername(username);
    }

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

    // ArticleService.java
    public long getArticleCount(String username) {
        // ArticleRepository에 countByAccountUsername 메서드가 있다고 가정합니다.
        // 없다면 아래처럼 작성하세요.
        return articleRepository.countByAccount_Username(username);
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

    public long countStudyingProgress(String username) {
        return studyProgressRepository.countByAccountUsernameAndStatus(username, ProgressStatus.STUDYING);
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

    @Transactional(readOnly = true)
    public long countAllArticles() {
        return articleRepository.count(); // 전체 글 개수
    }

    @Transactional(readOnly = true)
    public long countArticlesByTag(String tagName) {
        // 태그별 검색 결과 개수 반환
        return articleTagRepository.findByTagTagName(tagName).size();
    }
}