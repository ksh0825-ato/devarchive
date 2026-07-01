package com.devarchive.devarchive.dto.jobpost;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Getter/Setter/toString/equals 등을 자동 생성
@NoArgsConstructor
@AllArgsConstructor
@Builder // 객체 생성 시 데이터 적용을 편리하게 하는 Builder 패턴 제공
public class JobPostDto {

    private Long jobId; // 1. 여기서 정의

    private String jobPostTitle; // 공고 타이틀

    private String companyName; // 회사명
    private String position; // 채용 중인 직무(직책)명
    // ex) 백엔드 개발자, 데이터 엔지니어를 구합니다 등등
    private String description; // 설명
    private String url; // 링크
    
    @DateTimeFormat(pattern = "yyyy-MM-dd") // 이 부분을 추가하세요
    private LocalDate deadline; // 마감일

    @CreationTimestamp
    private LocalDateTime createdAt; // 공고 작성 시간
}