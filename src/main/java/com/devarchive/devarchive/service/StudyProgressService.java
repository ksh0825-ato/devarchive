package com.devarchive.devarchive.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devarchive.devarchive.domain.StudyProgress;
import com.devarchive.devarchive.domain.StudyProgress.ProgressStatus;
import com.devarchive.devarchive.repository.StudyProgressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

@Transactional(readOnly = true)
public class StudyProgressService {
    private final StudyProgressRepository studyProgressRepository;

    // 1. 전체 조회
    public List<StudyProgress> findAll() {
        return studyProgressRepository.findAll();
    }


    // 2-1. 특정 상태(STUDYING 등)의 전체 개수 조회 (통계용)
    public long countByStatus(ProgressStatus status) {
        return studyProgressRepository.countByStatus(status);
    }

    // 2-2. 유저의 진행 상황 개수 조회(username 기준)
    public long countStudyingByUsername(String username) {
    // 1. 해당 유저의 모든 진행 상황을 가져온 뒤
    List<StudyProgress> progressList = studyProgressRepository.findByAccountUsername(username);
    
    // 2. 메모리에서 필터링 (가장 안전하고 확실한 방법)
    return progressList.stream()
            .filter(p -> p.getStatus() == ProgressStatus.STUDYING)
            .count();
    }


    // 3-1. 유저의 모든 학습 현황 조회(userId 기준)
    public List<StudyProgress> getProgressByUserId(Long userId) {
        return studyProgressRepository.findByAccountUserId(userId);
    }

    // 3-2. 유저의 모든 학습 현황 조회(userName 기준)
    public List<StudyProgress> getProgressByUsername(String username) {
        return studyProgressRepository.findByAccountUsernameCustom(username);
    }


    // 4. 학습 상태 변경 (Update 로직)
    @Transactional
    public void updateProgressStatus(Long progressId, ProgressStatus newStatus) {
        StudyProgress progress = studyProgressRepository.findById(progressId)
                .orElseThrow(() -> new IllegalArgumentException("진행 상황을 찾을 수 없습니다."));
        
        progress.setStatus(newStatus);
    }

}
