package com.devarchive.devarchive.dto.article;

import java.time.LocalDateTime;

import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.Visibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data // Getter, Setter, toString 등을 자동 생성
public class ArticleDto {

    private Long articleId; // 수정 시 필요

    @NotBlank(message = "학습글 제목을 입력해주세요.") // 빈칸 방지
    private String title; // 학습글 제목

    @NotBlank(message = "학습글 본문을 입력해주세요.") // 빈칸 방지
    private String content; // 본문
    
    private Long jobId; // 연결할 채용 공고 ID
    private Long viewCount;
    private LocalDateTime createdAt; // 생성일
    private LocalDateTime updatedAt; // 수정일

    @NotNull(message = "공개 여부를 선택해주세요.") // 빈 문자열이나 null 방지
    private Visibility visibility = Visibility.PRIVATE;

    // 1. 인자가 필요한 경우 사용하는 메서드
    public Article toEntity(String username, Long jobId) {
        Article article = new Article();
        article.setTitle(this.title);
        article.setContent(this.content);
        article.setVisibility(this.visibility);
        
        return article;
    }

    // 2. 인자 없이 호출될 때를 위한 기본 메서드 (에러 방지용)
    public Article toEntity() {
        return toEntity(null, null);
    }
}