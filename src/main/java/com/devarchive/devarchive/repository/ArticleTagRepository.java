package com.devarchive.devarchive.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.ArticleTag;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {
    List<ArticleTag> findByArticle(Article article);
}