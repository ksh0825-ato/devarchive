package com.devarchive.devarchive.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devarchive.devarchive.domain.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    @Query("SELECT a FROM Article a WHERE a.account.username = :username")
    Page<Article> findByAccount_Username(@Param("username") String username, Pageable pageable);

    // 검색용 메서드 추가
    @Query("SELECT a FROM Article a WHERE a.account.username = :username AND a.title LIKE %:keyword%")
    Page<Article> findByAccount_UsernameAndTitleContaining(@Param("username") String username, @Param("keyword") String keyword, Pageable pageable);

    // Fetch Join을 사용하여 Article을 조회할 때 JobPost를 한 번에 가져옴
    @Query("SELECT a FROM Article a JOIN FETCH a.jobPost WHERE a.articleId = :articleId")
    Optional<Article> findByIdWithJobPost(@Param("articleId") Long articleId);

    @Query("SELECT j.companyName, COUNT(a) FROM Article a JOIN a.jobPost j WHERE a.account.username = :username GROUP BY j.companyName ORDER BY COUNT(a) DESC")
    List<Object[]> findTopCompanies(@Param("username") String username);

    // 유저의 전체 학습 기록 수
    long countByAccount_Username(String username);
    
    // 유저가 학습 기록을 남긴 공고 개수 (중복 제외)
    @Query("SELECT COUNT(DISTINCT a.jobPost) FROM Article a WHERE a.account.username = :username AND a.jobPost IS NOT NULL")
    long countDistinctJobByAccount_Username(@Param("username") String username);

}