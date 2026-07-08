package com.devarchive.devarchive.domain;

// 태그 관리

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
@AllArgsConstructor // <-- 이 줄을 추가하세요! (모든 필드 생성자 자동 생성)
@Builder // 빌더 패턴 사용 가능하게 설정
public class Skill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 보통 id라고 씁니다.

    @Column(nullable = false)
    private String name; // 여기에 'JAVA', 'MySQL', 'Spring Boot' 등이 들어갑니다.
    
    // 추가: 이름만 받는 생성자
    public Skill(String name) {
        this.name = name;
    }

}