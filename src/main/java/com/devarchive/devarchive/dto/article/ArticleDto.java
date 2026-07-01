package com.devarchive.devarchive.dto.article;

import lombok.Data;
import java.time.LocalDateTime;

import com.devarchive.devarchive.domain.Article;

@Data // Getter, Setter, toString 등을 자동 생성
public class ArticleDto {
    
    private Long articleId; // 수정 시 필요
    private String title; // 학습글 제목
    private String content; // 본문
    private Long jobId; // 연결할 채용 공고 ID
    private Long viewCount;
    private LocalDateTime createdAt; // 생성일
    private LocalDateTime updatedAt; // 수정일

    // 엔티티로 변환하는 메서드 (선택 사항)
    public Article toEntity() {
        return Article.builder()
                .title(this.title)
                .content(this.content)
                .build();
    }
}