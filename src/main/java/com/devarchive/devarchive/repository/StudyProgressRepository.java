package com.devarchive.devarchive.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.domain.StudyProgress;
import com.devarchive.devarchive.domain.StudyProgress.ProgressStatus;


// 사용자가 특정 공고(JobPost)를 어떤 상태(ProgressStatus)로 학습하고 있는지 관리하는 핵심 저장소

@Repository
public interface StudyProgressRepository extends JpaRepository<StudyProgress, Long> {
    
    // 1-1. 유저의 전체 학습 목록 조회 (Spring Data JPA 관례)
    // Account 엔티티의 username 필드를 기준으로 학습 기록 리스트를 가져옴
    List<StudyProgress> findByAccountUsername(String username);
    
    // 1-2. 유저의 전체 학습 목록 조회 (ID 기반)
    // Account의 기본 키(userId)를 사용하여 학습 기록 리스트를 가져옴
    List<StudyProgress> findByAccountUserId(Long userId);
    
    
    // 2-1. 유저의 전체 학습 목록 조회 (JPQL 버전)
    // JOIN을 명시적으로 사용한 버전 (성능 최적화 또는 복잡한 조건 추가 시 유리)
    @Query("SELECT sp FROM StudyProgress sp JOIN sp.account a WHERE a.username = :username")
    List<StudyProgress> findByAccountUsernameCustom(@Param("username") String username);
    
    // 2-2. 특정 학습 기록 단건 조회
    // 특정 유저가 특정 공고에 대해 어떤 상태인지 상세하게 확인 (수정/업데이트 시 사용)
    Optional<StudyProgress> findByAccountUsernameAndJobPost(String username, JobPost jobPost);
    
    // 2-3. 특정 공고에 대한 학습자들 조회
    // 해당 공고(JobPost)를 공부하고 있는 모든 사람들의 학습 기록을 조회
    List<StudyProgress> findByJobPost_JobId(Long jobId);


    // 3-1. 유저별 상태별 통계 조회
    // 특정 사용자의 특정 학습 상태(예: 학습 중)인 공고 개수를 카운트 (통계 카드용)
    long countByAccountUsernameAndStatus(String username, ProgressStatus progressStatus);
    
    // 3-2. 전체 통계 조회
    // 시스템 전체에서 특정 학습 상태(예: 완료됨)인 데이터가 몇 개인지 카운트
    long countByStatus(ProgressStatus status);


    // 4. 중복 학습 기록 체크
    // 해당 사용자가 특정 공고를 이미 학습 중인지 확인 (글 작성 시 중복 생성 방지용)  
    boolean existsByAccountAndJobPost(Account account, JobPost jobPost);

    
    // 5. 학습 기록 삭제
    // 게시글 삭제 시 연결된 학습 기록도 함께 삭제할 때 사용 
    void deleteByAccountAndJobPost(Account account, JobPost jobPost);
}