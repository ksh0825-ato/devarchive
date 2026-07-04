package com.devarchive.devarchive.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.JobPost;

@Repository // 이 어노테이션이 있어야 Spring이 빈으로 인식합니다

// JobPost 엔티티를 다루는 인터페이스
public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    Optional<JobPost> findByCompanyName(String companyName);

    // 마감일이 오늘 이후인 공고를 마감일 순으로 5개만 가져오기
    List<JobPost> findTop5ByDeadlineAfterOrderByDeadlineAsc(LocalDate today, Pageable pageable);
}