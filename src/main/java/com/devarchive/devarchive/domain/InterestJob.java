package com.devarchive.devarchive.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor // 기본 생성자 유지
public class InterestJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private JobPost jobPost;

    // 명시적 생성자 추가
    public InterestJob(Long userId, JobPost jobPost) {
        this.userId = userId;
        this.jobPost = jobPost;
    }
}