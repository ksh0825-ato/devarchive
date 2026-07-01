package com.devarchive.devarchive.service;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.InterestJob;
import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.dto.jobpost.JobPostDto;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.repository.InterestJobRepository;
import com.devarchive.devarchive.repository.JobPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class JobPostService {
    private final JobPostRepository jobPostRepository;
    private final AccountRepository accountRepository;
    private final InterestJobRepository interestJobRepository;

// 공고 등록을 위한 통합 메서드
    @Transactional
    public void registerJobPost(JobPostDto dto, String username) {
        System.out.println("서비스 진입 - username: " + username);
        // 1. 현재 로그인한 사용자(Account)를 DB에서 조회
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));

        // 2. DTO -> 엔티티 변환 (Account를 직접 세팅)
        JobPost jobPost = JobPost.builder()
                .account(account) // 여기서 FK(user_id)가 연결됩니다.
                .jobId(dto.getJobId()) // ★ 이 부분이 반드시 있어야 합니다!
                .jobPostTitle(dto.getJobPostTitle())
                .companyName(dto.getCompanyName())
                .position(dto.getPosition())
                .description(dto.getDescription())
                .url(dto.getUrl())
                .deadline(dto.getDeadline())
                .build();

        jobPostRepository.save(jobPost);
        System.out.println("저장 완료! (연결된 유저: " + account.getUsername() + ")");
     }
    
    // 2. 관심 공고 추가
    public void addInterestJob(Account account, Long jobPostId) {
        JobPost jobPost = jobPostRepository.findById(jobPostId).orElseThrow();
        InterestJob interest = new InterestJob();
        interest.setAccount(account);
        interest.setJobPost(jobPost);
        interestJobRepository.save(interest);
    }
    
    // 게시글 조회
    // 페이징 처리된 목록 조회
    public Page<JobPost> getJobPostList(Pageable pageable) {
        return jobPostRepository.findAll(pageable);
    }

    // 공고 게시글 상세 조회
    public JobPostDto getJobPostById(Long jobId) {
        // 1. repository에서 해당 id의 공고를 찾고, 없으면 예외 발생
        JobPost jobPost = jobPostRepository.findById(jobId)
                        .orElseThrow(() -> new IllegalArgumentException("해당 공고가 존재하지 않습니다. ID: " + jobId));
        // 2. 조회된 엔티티를 DTO로 변환하여 반환
        return convertToDto(jobPost);
    }

    // DTO 변환 로직 (기존에 작성해두신 toDTO 혹은 비슷한 메서드 활용)
    private JobPostDto convertToDto(JobPost jobPost) {
        return JobPostDto.builder()
                .jobId(jobPost.getJobId())
                .jobPostTitle(jobPost.getJobPostTitle())
                .companyName(jobPost.getCompanyName())
                .createdAt(jobPost.getCreatedAt())
                .position(jobPost.getPosition())
                .description(jobPost.getDescription())
                .url(jobPost.getUrl())
                .deadline(jobPost.getDeadline())
                .build();
    }
    
    // 채용 공고 수정
    @Transactional
    public void updateJobPost(Long jobId, JobPostDto dto) {
        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공고가 없습니다."));
        
        // 엔티티의 값을 DTO로 받은 값으로 변경 (엔티티 내부에 update 메서드를 만드는 것이 좋습니다)
        jobPost.update(dto.getJobPostTitle(), dto.getCompanyName(), dto.getPosition(), 
                    dto.getDescription(), dto.getUrl(), dto.getDeadline());
    }

    // 채용 공고 삭제
    @Transactional
        public void deleteJobPost(Long jobId) {
        jobPostRepository.deleteById(jobId);
    }

    // jobPost save 메소드
    public void save(JobPost jobPost) {
        jobPostRepository.save(jobPost);
    }

    // 모든 채용 공고를 조회하는 메서드
    @Transactional(readOnly = true)
    public List<JobPost> getAllJobPosts() {
        return jobPostRepository.findAll();
    }

}