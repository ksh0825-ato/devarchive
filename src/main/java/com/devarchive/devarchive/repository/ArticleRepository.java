package com.devarchive.devarchive.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.devarchive.devarchive.domain.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    @Query("SELECT a FROM Article a WHERE a.account.username = :username")
    Page<Article> findByAccount_Username(@Param("username") String username, Pageable pageable);

    // 검색용 메서드 추가
    @Query("SELECT a FROM Article a WHERE a.account.username = :username AND a.title LIKE %:keyword%")
    Page<Article> findByAccount_UsernameAndTitleContaining(@Param("username") String username, @Param("keyword") String keyword, Pageable pageable);
}