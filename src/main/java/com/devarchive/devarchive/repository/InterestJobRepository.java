package com.devarchive.devarchive.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devarchive.devarchive.domain.InterestJob;

public interface InterestJobRepository extends JpaRepository<InterestJob, Long> {
    // 유저와 공고 아이디로 찜 정보를 찾음
    Optional <InterestJob> findByUserIdAndJobPostJobId(Long userId, Long jobId);
    
    // 찜 여부 확인
    boolean existsByUserIdAndJobPostJobId(Long userId, Long jobId);

    List<InterestJob> findByUserId(Long userId);
}