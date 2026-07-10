package com.devarchive.devarchive.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleTag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id") // FK
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id") // FK
    private Tag tag;

    // Tag 객체를 반환하는 메서드
    public Tag getTag() {
        return tag;
    }

    // Article 객체를 반환하는 메서드
    public Article getArticle() {
        return article;
    }    

    public ArticleTag(Article article, Tag tag) {
        this.article = article;
        this.tag = tag;
    }
}
