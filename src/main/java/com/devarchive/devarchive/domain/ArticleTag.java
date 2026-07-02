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


// ARTICLE_TAG 등록 로직 (핵심 포인트)
// 태그는 사용자가 입력한 문자열을 바탕으로 Tag 테이블에 있는지 확인하고, 없으면 새로 만들고 연결해야 합니다.

// 로직 흐름:
// 1. 사용자가 글을 작성할 때 쉼표로 구분된 태그 문자열(예: "Java,Spring")을 받습니다.
// 2. 서비스에서 ,로 잘라낸 뒤 반복문을 돕니다.
// 3. 각 태그가 Tag 테이블에 존재하면 가져오고, 없으면 TagRepository.save()를 호출합니다.
// 4. 마지막으로 ArticleTag 객체를 생성하여 Article과 Tag를 연결합니다.

@NoArgsConstructor // 기본 생성자 (JPA 필수)
@AllArgsConstructor // 모든 필드를 받는 생성자 (이게 있으면 됩니다!)
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
    // 확인 필요
    public Article getArticle() {
        return article;
    }    

    // 만약 Lombok 대신 직접 작성하고 싶다면 아래 생성자를 넣으세요
    public ArticleTag(Article article, Tag tag) {
        this.article = article;
        this.tag = tag;
    }
}
