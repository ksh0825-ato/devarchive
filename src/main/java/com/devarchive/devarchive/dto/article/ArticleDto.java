package com.devarchive.devarchive.dto.article;

import java.time.LocalDateTime;

import com.devarchive.devarchive.domain.Article;

import lombok.Data;

@Data // Getter, Setter, toString 등을 자동 생성
public class ArticleDto {
    
    private Long articleId; // 수정 시 필요
    private String title; // 학습글 제목
    private String content; // 본문
    private Long jobId; // 연결할 채용 공고 ID
    private Long viewCount;
    private LocalDateTime createdAt; // 생성일
    private LocalDateTime updatedAt; // 수정일
    private String visibility; 

    // 1. 인자가 필요한 경우 사용하는 메서드
    public Article toEntity(String username, Long jobId) {
        Article article = new Article();
        article.setTitle(this.title);
        article.setContent(this.content);
        
        // 여기에 username이나 jobId를 엔티티에 세팅하는 로직을 추가하세요.
        // 예: article.setAccount(account); 등
        
        return article;
    }

    // 2. 인자 없이 호출될 때를 위한 기본 메서드 (에러 방지용)
    public Article toEntity() {
        return toEntity(null, null);
    }
}