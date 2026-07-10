package com.devarchive.devarchive.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPost {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId; // PK

    @ManyToOne
    @JoinColumn(name = "user_id") // FK
    private Account account;

    private String jobPostTitle; // 공고 타이틀

    private String companyName; // 회사명
    private String position; // 채용 중인 직무(직책)명
    // ex) 백엔드 개발자, 데이터 엔지니어를 구합니다 등등
    private String description; // 설명
    private String url; // 링크
    private LocalDate deadline; // 마감일

    @CreationTimestamp // 처음 생성될 때 시간 자동 생성
    private LocalDateTime createdAt; // 공고 작성 시간


    // article 쌍방향 연결
    @OneToMany(mappedBy = "jobPost")
    private List<Article> articles = new ArrayList<>();
    

    // update 메서드(채용 공고 수정)
    public void update(String title, String company, String position, String desc, String url, LocalDate deadline) {
        this.jobPostTitle = title;
        this.companyName = company;
        this.position = position;
        this.description = desc;
        this.url = url;
        this.deadline = deadline;
    }
    
    
    @ManyToMany
    @JoinTable(name = "job_post_skill")
    private List<Skill> skills = new ArrayList<>();
    
}