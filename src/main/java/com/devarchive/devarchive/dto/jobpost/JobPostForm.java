package com.devarchive.devarchive.dto.jobpost;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
// 사용자가 공고를 등록할 때 데이터를 주고받기 위한 객체, DTO
public class JobPostForm {
    private String jobPostTitle;
    private String companyName;
    private String description;
    
    // 체크박스로 선택된 Skill들의 ID를 담을 리스트
    // html의 th:field="*{skills}"와 매핑됩니다.
    private List<Long> skills = new ArrayList<>();
}