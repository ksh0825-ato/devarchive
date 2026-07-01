package com.devarchive.devarchive.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.JobPost;

@Repository // 이 어노테이션이 있어야 Spring이 빈으로 인식합니다

// JobPost 엔티티를 다루는 인터페이스
public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    Optional<JobPost> findByCompanyName(String companyName);
}