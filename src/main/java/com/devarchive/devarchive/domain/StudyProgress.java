package com.devarchive.devarchive.domain;

import java.time.LocalDateTime;

import com.devarchive.devarchive.domain.StudyProgress.ProgressStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Builder
public class StudyProgress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressId; // PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = true) // null이 가능해야 연결을 끊을 수 있음
    private JobPost jobPost;

    @ManyToOne
    @JoinColumn(name = "user_id") // FK
    private Account account;

    @Enumerated(EnumType.STRING)
    private ProgressStatus status;

    private LocalDateTime updatedAt = LocalDateTime.now();

    // 클래스 내부 맨 아래에 정의
    public enum ProgressStatus {
        NOT_STARTED, STUDYING, COMPLETED
    }
}