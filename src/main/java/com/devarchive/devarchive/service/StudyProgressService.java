package com.devarchive.devarchive.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.domain.StudyProgress;
import com.devarchive.devarchive.domain.StudyProgress.ProgressStatus;
import com.devarchive.devarchive.repository.StudyProgressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

@Transactional(readOnly = true)
public class StudyProgressService {

    private final StudyProgressRepository studyProgressRepository;

    // 1. 사용자 ID로 모든 학습 현황 조회
    public List<StudyProgress> getProgressByUserId(Long userId) {
        return studyProgressRepository.findByAccountUserId(userId);
    }

    // 2. 특정 상태(STUDYING 등)의 전체 개수 조회 (통계용)
    public long countByStatus(ProgressStatus status) {
        return studyProgressRepository.countByStatus(status);
    }

    // 3. 학습 상태 변경 (Update 로직)
    @Transactional
    public void updateProgressStatus(Long progressId, ProgressStatus newStatus) {
        StudyProgress progress = studyProgressRepository.findById(progressId)
                .orElseThrow(() -> new IllegalArgumentException("진행 상황을 찾을 수 없습니다."));
        
        progress.setStatus(newStatus);
        // updated_at은 엔티티에서 자동으로 갱신되거나 필요시 여기에서 처리
    }

    public List<StudyProgress> findAll() {
        return studyProgressRepository.findAll();
    }

}
