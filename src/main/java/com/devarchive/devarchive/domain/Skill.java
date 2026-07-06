package com.devarchive.devarchive.domain;

// 태그 관리

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Skill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 보통 id라고 씁니다.

    @Column(nullable = false)
    private String name; // 여기에 'JAVA', 'MySQL', 'Spring Boot' 등이 들어갑니다.
}