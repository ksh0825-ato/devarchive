package com.devarchive.devarchive.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.ArticleTag;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {
    // 태그 이름(Tag의 tagName)을 기준으로 조회
    List<ArticleTag> findByTag_TagName(String tagName);

    // 특정 Article에 연결된 모든 ArticleTag 리스트를 조회
    List<ArticleTag> findByArticle(Article article);
}