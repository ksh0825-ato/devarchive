package com.devarchive.devarchive.dto.jobpost;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostDto {

    private Long jobId;

    @NotBlank(message = "공고 제목을 입력해주세요.")
    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하로 작성해주세요.")
    private String jobPostTitle; // 공고 타이틀

    @NotBlank(message = "기업명을 입력해주세요.")
    private String companyName; // 회사명

    @NotBlank(message = "직무명을 입력해주세요.")
    private String position; // 채용 중인 직무(직책)명
    // ex) 백엔드 개발자, 데이터 엔지니어를 구합니다 등등

    @NotBlank(message = "공고 설명을 입력해주세요.")
    @Size(min = 10, message = "내용은 최소 10자 이상 작성해주세요.")
    private String description; // 설명

    @NotBlank(message = "홈페이지 URL을 입력해주세요.")
    private String url; // 링크
    
    @jakarta.validation.constraints.NotNull(message = "마감일을 선택해주세요.") // null 방지
    @FutureOrPresent(message = "마감일은 오늘 이후로 설정해주세요.") // 과거 날짜 방지
    @DateTimeFormat(pattern = "yyyy-MM-dd") // 이 부분을 추가하세요
    private LocalDate deadline; // 마감일

    @NotBlank(message = "사용 스킬을 입력해주세요.")
    private List<Long> skills;

    @CreationTimestamp
    private LocalDateTime createdAt; // 공고 작성 시간

}