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

    @Query("SELECT a FROM Article a WHERE a.account.username = :username AND a.title LIKE %:keyword%")
    Page<Article> findByAccount_UsernameAndTitleContaining(@Param("username") String username, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a FROM Article a JOIN FETCH a.jobPost WHERE a.articleId = :articleId")
    Optional<Article> findByIdWithJobPost(@Param("articleId") Long articleId);

    @Query("SELECT j.companyName, COUNT(a) FROM Article a JOIN a.jobPost j WHERE a.account.username = :username GROUP BY j.companyName ORDER BY COUNT(a) DESC")
    List<Object[]> findTopCompanies(@Param("username") String username);

    long countByAccount_Username(String username);
    
    @Query("SELECT COUNT(DISTINCT a.jobPost) FROM Article a WHERE a.account.username = :username AND a.jobPost IS NOT NULL")
    long countDistinctJobByAccount_Username(@Param("username") String username);

    
}