package com.devarchive.devarchive.service;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.InterestJob;
import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.domain.StudyProgress;
import com.devarchive.devarchive.dto.jobpost.JobPostDto;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.repository.ArticleRepository;
import com.devarchive.devarchive.repository.InterestJobRepository;
import com.devarchive.devarchive.repository.JobPostRepository;
import com.devarchive.devarchive.repository.StudyProgressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class JobPostService {
    private final JobPostRepository jobPostRepository;
    private final AccountRepository accountRepository;
    private final InterestJobRepository interestJobRepository;
    private final ArticleRepository articleRepository;
    private final StudyProgressRepository studyProgressRepository;

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

    public List<JobPost> findUrgentJobs() {
        return jobPostRepository.findAll().stream()
                // deadline이 오늘 이후인 공고만 필터링
                .filter(job -> job.getDeadline() != null && job.getDeadline().isAfter(LocalDate.now()))
                // 마감일이 빠른 순서대로 정렬
                .sorted(Comparator.comparing(JobPost::getDeadline))
                // 5개까지만
                .limit(5)
                .collect(Collectors.toList());
    }

    // D-DAY 계산
    public long calculateDDay(LocalDate deadline) {
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
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
        // 1. 해당 공고를 참조하는 모든 학습글(Article) 찾기
        List<Article> articles = articleRepository.findByJobPost_JobId(jobId);
        for (Article article : articles) {
            article.setJobPost(null); // 연결 해제
        }

        // 2. 학습 진행 상황(StudyProgress)도 연결 해제
        List<StudyProgress> progresses = studyProgressRepository.findByJobPost_JobId(jobId);
        for (StudyProgress progress : progresses) {
            progress.setJobPost(null); // 연결 해제
        }

        // 3. 이제 아무것도 참조하지 않으므로 공고만 안전하게 삭제
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