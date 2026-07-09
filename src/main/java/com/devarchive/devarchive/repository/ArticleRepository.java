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
    
    // 1. 페이징용 (목록 페이지에서 사용)
    @EntityGraph(attributePaths = {"articleTags", "articleTags.tag"})
    @Query("SELECT a FROM Article a WHERE a.account.username = :username")
    Page<Article> findByAccount_Username(@Param("username") String username, Pageable pageable);

    // 2. 전체 조회용 (메인 페이지 대시보드 카드에서 사용)
    List<Article> findByAccount_Username(String username);

    @Query("SELECT a FROM Article a WHERE a.account.username = :username AND a.title LIKE %:keyword%")
    Page<Article> findByAccount_UsernameAndTitleContaining(@Param("username") String username, @Param("keyword") String keyword, Pageable pageable);

    // 1. 페이징 처리를 위해 Page<Article> 반환
    // 2. @EntityGraph를 사용하여 연관된 태그 정보를 한 번의 쿼리(JOIN)로 가져옴 (N+1 문제 방지)
    @EntityGraph(attributePaths = {"articleTags", "articleTags.tag"})
    @Query("SELECT a FROM Article a WHERE a.visibility = 'PUBLIC' ORDER BY a.createdAt DESC")
    Page<Article> findAllPublicArticles(Pageable pageable);

    @Query("SELECT a FROM Article a JOIN FETCH a.jobPost WHERE a.articleId = :articleId")
    Optional<Article> findByIdWithJobPost(@Param("articleId") Long articleId);

    @Query("SELECT j.companyName, COUNT(a) FROM Article a JOIN a.jobPost j WHERE a.account.username = :username GROUP BY j.companyName ORDER BY COUNT(a) DESC")
    List<Object[]> findTopCompanies(@Param("username") String username);

    List<Article> findByJobPost_JobId(Long jobId);
    // 설명: findBy + JobPost(엔티티의 필드명) + _(경로 구분) + JobPostId(JobPost 엔티티 내의 ID 필드명) 순서

    long countByAccount_Username(String username);
    
    @Query("SELECT COUNT(DISTINCT a.jobPost) FROM Article a WHERE a.account.username = :username AND a.jobPost IS NOT NULL")
    long countDistinctJobByAccount_Username(@Param("username") String username);

    @Query("SELECT COUNT(DISTINCT a.jobPost.jobId) FROM Article a WHERE a.account.username = :username AND a.jobPost IS NOT NULL")
    long countUniqueJobPostsByUsername(@Param("username") String username);

    @Query("SELECT COUNT(a) FROM Article a WHERE a.account.username = :username")
    long countAllArticlesByUsername(@Param("username") String username);

    boolean existsByAccountAndJobPost(Account account, JobPost jobPost);

    List<Article> findByAccountUserIdAndVisibility(Long userId, Visibility visibility);

    // 1. 공유 게시판용: 공개된 글만 조회
    List<Article> findByVisibilityOrderByCreatedAtDesc(Visibility visibility);

    // 2. 내 목록용: 특정 유저의 모든 글 조회 (공개/비공개 상관없이)
    List<Article> findByAccountUserIdOrderByCreatedAtDesc(Long userId);
}