package com.devarchive.devarchive.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class StudyProgress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressId; // PK

    @ManyToOne
    @JoinColumn(name = "job_id") // FK
    private JobPost jobPost;

    @ManyToOne
    @JoinColumn(name = "user_id") // FK
    private Account account;

@Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime updatedAt = LocalDateTime.now();

    // 클래스 내부 맨 아래에 정의
    public enum Status {
        NOT_STARTED, STUDYING, COMPLETED
    }
}