package com.devarchive.devarchive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devarchive.devarchive.domain.StudyProgress;
import com.devarchive.devarchive.domain.StudyProgress.ProgressStatus;

import java.util.List;

@Repository
public interface StudyProgressRepository extends JpaRepository<StudyProgress, Long> {
    
    // 특정 상태(STUDYING, COMPLETED 등)의 개수를 조회하는 메서드
    long countByStatus(ProgressStatus status);
    
    // Account 엔티티 내의 userId 필드를 기준으로 조회
    List<StudyProgress> findByAccountUserId(Long userId);
}