package com.devarchive.devarchive.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.domain.Visibility;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // 1. 페이징 조회 (Fetch Join으로 N+1 문제 해결)
    // 연관된 articleTags와 그 안의 tag 엔티티까지 한 번에 JOIN하여 가져옴
    @EntityGraph(attributePaths = {"articleTags", "articleTags.tag"})
    @Query("SELECT a FROM Article a WHERE a.account.username = :username")
    Page<Article> findByAccount_Username(@Param("username") String username, Pageable pageable);

    // 2. 전체 공개 게시글 페이징 조회
    // 최신순으로 정렬된 공개 게시글만 조회하며, 태그 정보를 함께 로딩
    @EntityGraph(attributePaths = {"articleTags", "articleTags.tag"})
    @Query("SELECT a FROM Article a WHERE a.visibility = 'PUBLIC' ORDER BY a.createdAt DESC")
    Page<Article> findAllPublicArticles(Pageable pageable);

    // 3. 특정 유저의 전체 게시글 목록 조회 (일반 리스트)
    List<Article> findByAccount_Username(String username);

    
    // 4. 유저별 키워드 검색 (제목 포함)
    // 특정 유저가 작성한 글 중 제목에 키워드가 포함된 항목만 페이징 조회
    @Query("SELECT a FROM Article a WHERE a.account.username = :username AND a.title LIKE %:keyword%")
    Page<Article> findByAccount_UsernameAndTitleContaining(@Param("username") String username, @Param("keyword") String keyword, Pageable pageable);

    // 5. 공고 정보가 포함된 상세 게시글 조회
    // Join Fetch를 사용하여 지연 로딩을 방지하고 즉시 공고(JobPost) 정보까지 가져옴
    @Query("SELECT a FROM Article a JOIN FETCH a.jobPost WHERE a.articleId = :articleId")
    Optional<Article> findByIdWithJobPost(@Param("articleId") Long articleId);

    // 6. 특정 공고와 연결된 모든 게시글 조회
    List<Article> findByJobPost_JobId(Long jobId);


    // 7. 통계용 카운트 메서드들
    // 7-1. 유저가 작성한 총 게시글 수
    long countByAccount_Username(String username);
    
    // 7-2. 유저가 분석 중인 고유한 공고 수 (DISTINCT를 사용하여 중복 제거)
    @Query("SELECT COUNT(DISTINCT a.jobPost.jobId) FROM Article a WHERE a.account.username = :username AND a.jobPost IS NOT NULL")
    long countUniqueJobPostsByUsername(@Param("username") String username);
    
    // 7-3. 유저의 전체 게시글 수 (명시적 JPQL 버전)
    @Query("SELECT COUNT(a) FROM Article a WHERE a.account.username = :username")
    long countAllArticlesByUsername(@Param("username") String username);


    // 8. 특정 유저가 특정 공고에 대해 게시글을 썼는지 확인
    boolean existsByAccountAndJobPost(Account account, JobPost jobPost);


    // 9. 공개 범위별 조회
    // 유저 ID와 공개 범위(PUBLIC/PRIVATE)를 기준으로 목록 조회
    List<Article> findByAccountUserIdAndVisibility(Long userId, Visibility visibility);


    // 10. 공유 게시판(전체 공개) 및 내 대시보드 조회
    List<Article> findByVisibilityOrderByCreatedAtDesc(Visibility visibility);
    List<Article> findByAccountUserIdOrderByCreatedAtDesc(Long userId);
}