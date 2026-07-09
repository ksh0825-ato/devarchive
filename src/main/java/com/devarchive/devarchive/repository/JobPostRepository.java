package com.devarchive.devarchive.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.JobPost;

@Repository // 이 어노테이션이 있어야 Spring이 빈으로 인식합니다

// JobPost 엔티티를 다루는 인터페이스
public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    Optional<JobPost> findByCompanyName(String companyName);

    List<JobPost> findByJobPostTitle(String title);

    // 마감일(deadline)이 오늘 이후인 것 중, 가까운 순으로 5개
        @Query("SELECT j FROM JobPost j WHERE j.deadline >= CURRENT_DATE ORDER BY j.deadline ASC")
        List<JobPost> findTop5ByDeadlineAfterOrderByDeadlineAsc(Pageable pageable);

    // 작성일(createdAt) 기준 내림차순(최신순)으로 5개를 가져오는 메서드
    List<JobPost> findTop5ByOrderByCreatedAtDesc(Pageable pageable);

}