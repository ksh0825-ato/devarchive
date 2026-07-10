package com.devarchive.devarchive.service;

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

// 게시글(Article), 태그(Tag), 그리고 공고(JobPost)와 학습 상태(StudyProgress) 간의
// 복잡한 연관 관계를 조정하는 핵심 비즈니스 로직 클래스

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final StudyProgressRepository studyProgressRepository;
    private final ArticleRepository articleRepository;
    private final AccountRepository accountRepository;
    private final JobPostRepository jobPostRepository;
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;
    
    
    // 1. 유저별 게시글 페이지네이션 조회 (목록 페이지용)
    public Page<Article> findArticlesByUsername(String username, Pageable pageable) {
        return articleRepository.findByAccount_Username(username, pageable);
    }


    // 2. 유저별 게시글 전체 목록 조회
    public List<Article> findAllByUsername(String username){
        return articleRepository.findByAccount_Username(username);
    }


    // 3. 게시글 작성 및 학습 상태 관리
    @Transactional
    public Article saveArticle(ArticleDto articleDto, String username, Long jobId) {
        // 1. 작성자 확인 -> 2. 엔티티 변환 -> 3. 공고가 있으면 연결
        // 4. 공고가 연결된 경우, 해당 유저가 처음 공부하는 공고라면 StudyProgress에 'STUDYING' 상태로 자동 등록

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


    // 4. ID로 게시글 단건 조회 (상세보기)
    @Transactional(readOnly = true)
    public Article findById(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + articleId));
    }


    // 5-1. 게시글 수정 (영속성 컨텍스트의 변경 감지 기능을 활용)
    @Transactional
    public void update(Long articleId, ArticleDto articleDto) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        
        article.setTitle(articleDto.getTitle());
        article.setContent(articleDto.getContent());
        article.setUpdatedAt(LocalDateTime.now());
        
        if (articleDto.getVisibility() != null) {
            article.setVisibility(articleDto.getVisibility());
        }
    }


    //5-2. 게시글 수정 및 태그 재매핑
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


    // 6-1. 학습 기록 전체 조회
    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    // 6-2. 공개 학습 기록 전체 조회
    @Transactional(readOnly = true)
    public Page<Article> findAllPublicArticles(Pageable pageable) {
        return articleRepository.findAllPublicArticles(pageable);
    }
    

    // 7.  게시글 삭제 및 연결된 학습 상태 정리 (무결성 유지)
    @Transactional
    public void deleteArticle(Long articleId, String username) {
        // 1. 삭제 권한 체크 -> 2. 해당 유저가 작성한 다른 글이 더 이상 없다면 StudyProgress 기록 삭제
        // ... 3. 마지막으로 게시글 삭제 ...

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

    
    // 8. 유저가 분석 중인 고유 공고 수 조회 (통계용)
    @Transactional(readOnly = true)
    public long getUniqueJobCount(String username) {
        // 공고가 연결된 게시글들의 job_id를 중복 제거하여 카운트
        return articleRepository.countUniqueJobPostsByUsername(username);
    }


    // 9. 게시글별 전체 개수 조회
    public long getArticleCount(String username) {
        return articleRepository.countByAccount_Username(username);
    }


    // 10-1. 태그 저장 로직 (쉼표 구분자로 들어온 태그들을 파싱하여 DB 저장 및 연관 매핑)
    @Transactional
    public void saveTagsForArticle(Article article, String tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;
        
        String[] tags = tagNames.split(",");
        for (String name : tags) {
            String cleanName = name.trim();
            if (cleanName.isEmpty()) continue;
            
            // 1. 태그가 없으면 생성, 있으면 가져오기
            Tag tag = tagRepository.findByTagName(cleanName)
                    .orElseGet(() -> tagRepository.save(new Tag(cleanName)));
            
            // 2. 매핑 테이블(ARTICLE_TAG) 저장
            articleTagRepository.save(new ArticleTag(article, tag));
        }
    }

    // 10-2. 태그 이름으로 해당 태그가 달린 모든 게시글 조회
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


    // 10-3. 게시글에 등록된 모든 태그 객체 목록 조회
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


    // 11-1. 유저의 학습 진행 중인 공고 개수 조회
    public long countStudyingProgress(String username) {
        return studyProgressRepository.countByAccountUsernameAndStatus(username, ProgressStatus.STUDYING);
    }

    // 12-2. 전체 공부 기록 개수
    @Transactional(readOnly = true)
    public long countAllArticles() {
        return articleRepository.count();
    }

    // 13-3. 전체 공부 기록 개수(태그 검색 결과)
    @Transactional(readOnly = true)
    public long countArticlesByTag(String tagName) {
        // 태그별 검색 결과 개수 반환
        return articleTagRepository.findByTagTagName(tagName).size();
    }
}