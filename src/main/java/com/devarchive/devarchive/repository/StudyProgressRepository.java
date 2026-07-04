package com.devarchive.devarchive.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.domain.StudyProgress;
import com.devarchive.devarchive.domain.StudyProgress.ProgressStatus;

@Repository
public interface StudyProgressRepository extends JpaRepository<StudyProgress, Long> {
    
    // 특정 상태(STUDYING, COMPLETED 등)의 개수를 조회하는 메서드
    long countByStatus(ProgressStatus status);
    
    long countByAccountUsernameAndStatus(String username, ProgressStatus progressStatus);

    // 추가: 계정명(username)으로 학습 진행 상황 조회
    // Account 엔티티의 username 필드를 탐색하여 관련 StudyProgress를 가져옵니다.
    List<StudyProgress> findByAccountUsername(String username);

    @Query("SELECT sp FROM StudyProgress sp JOIN sp.account a WHERE a.username = :username")
    List<StudyProgress> findByAccountUsernameCustom(@Param("username") String username);

    List<StudyProgress> findByJobPost_JobId(Long jobId);

    List<StudyProgress> findByAccountUserId(Long userId);

    boolean existsByAccountAndJobPost(Account account, JobPost jobPost);

    void deleteByAccountAndJobPost(Account account, JobPost jobPost);
}