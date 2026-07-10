package com.devarchive.devarchive.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devarchive.devarchive.domain.InterestJob;
import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.repository.InterestJobRepository;
import com.devarchive.devarchive.repository.JobPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InterestJobService {
    private final InterestJobRepository interestJobRepository;
    private final JobPostRepository jobPostRepository;

    // 1. 관심 공고 메서드
    @Transactional
    public boolean toggleInterest(Long userId, Long jobId) {
        Optional<InterestJob> existing = interestJobRepository.findByUserIdAndJobPostJobId(userId, jobId);
        
        if (existing.isPresent()) {
            // 이미 있으면 삭제 (관심 취소)
            interestJobRepository.delete(existing.get());
            return false; // 취소 완료
        } else {
            // 없으면 저장 (찜하기)
            JobPost jobPost = jobPostRepository.findById(jobId).orElseThrow();
            InterestJob interestJob = new InterestJob(userId, jobPost);
            interestJobRepository.save(interestJob);
            return true; // 관심 완료
        }
    }
    
    // 2. UserId로 모든 관심 공고 가져오기
    @Transactional(readOnly = true) // 읽기 전용으로 성능 최적화
    public List<InterestJob> findAllByUserId(Long userId) {
        return interestJobRepository.findByUserId(userId);
    }

}