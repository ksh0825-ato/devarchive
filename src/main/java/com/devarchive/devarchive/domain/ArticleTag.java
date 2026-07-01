package com.devarchive.devarchive.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
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


public class ArticleTag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @ManyToOne
    @JoinColumn(name = "article_id") // FK
    private Article article;

    @ManyToOne
    @JoinColumn(name = "tag_id") // FK
    private Tag tag;
}
