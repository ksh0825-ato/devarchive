package com.devarchive.devarchive.dto.jobpost;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
// 사용자가 공고를 등록할 때 데이터를 주고받기 위한 객체, DTO
public class JobPostForm {

    @NotBlank(message = "공고 제목은 필수 입력 항목입니다.")
    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하로 작성해주세요.")
    private String jobPostTitle;

    @NotBlank(message = "기업명은 필수 입력 항목입니다.")
    private String companyName;

    @NotBlank(message = "공고 설명은 필수 입력 항목입니다.")
    @Size(min = 10, message = "내용은 최소 10자 이상 작성해주세요.")
    private String description;
    
    // 체크박스로 선택된 Skill들의 ID를 담을 리스트
    // html의 th:field="*{skills}"와 매핑됩니다.
    @NotEmpty(message = "최소 하나 이상의 기술(Skill)을 선택해주세요.")
    private List<Long> skills = new ArrayList<>();
}