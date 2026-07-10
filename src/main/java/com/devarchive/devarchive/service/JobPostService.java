package com.devarchive.devarchive.service;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.InterestJob;
import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.domain.Skill;
import com.devarchive.devarchive.domain.StudyProgress;
import com.devarchive.devarchive.dto.jobpost.JobPostDto;
import com.devarchive.devarchive.dto.jobpost.JobPostForm;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.repository.ArticleRepository;
import com.devarchive.devarchive.repository.InterestJobRepository;
import com.devarchive.devarchive.repository.JobPostRepository;
import com.devarchive.devarchive.repository.SkillRepository;
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
    private final SkillRepository skillRepository;


    // 1. 공고 등록 (통합 로직)
    // - 사용자 조회 후, DTO의 데이터를 엔티티로 변환하여 저장
    // - 기술 스택(Skill) 리스트를 조회하여 연관 관계 설정
    @Transactional
    public void registerJobPost(JobPostDto dto, String username) {

        // 1. 현재 로그인한 사용자(Account)를 DB에서 조회
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));

        // 2. DTO -> 엔티티 변환 (Account를 직접 세팅)
        JobPost jobPost = JobPost.builder()
                .account(account) // 여기서 FK(user_id)가 연결됨
                .jobId(dto.getJobId())
                .jobPostTitle(dto.getJobPostTitle())
                .companyName(dto.getCompanyName())
                .position(dto.getPosition())
                .description(dto.getDescription())
                .url(dto.getUrl())
                .deadline(dto.getDeadline())
                .build();

            if (dto.getSkills() != null && !dto.getSkills().isEmpty()) {
                List<Skill> selectedSkills = skillRepository.findAllById(dto.getSkills());
                jobPost.setSkills(selectedSkills);
            }

        jobPostRepository.save(jobPost);
    }
    

    // 2. 채용 공고 수정
    // - 스킬 정보를 새로 받아 기존 정보를 삭제하고 다시 연결 (Dirty Checking)
    @Transactional
    public void updateJobPost(Long jobId, JobPostForm form, JobPost job) {
        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공고가 없습니다."));
                
        // 1. 스킬 연관관계 업데이트 (ID 리스트를 받아 엔티티 조회 후 연결)
        List<Skill> newSkills = skillRepository.findAllById(form.getSkills());
        jobPost.getSkills().clear();
        jobPost.getSkills().addAll(newSkills);
        
        // 2. 나머지 필드 업데이트
        jobPost.update(form.getJobPostTitle(), form.getCompanyName(), 
                    job.getPosition(), form.getDescription(), 
                    job.getUrl(), job.getDeadline());
    }


    // 3. 공고 저장 (단순 저장용)
    // - 스킬 정보를 연결한 후 레포지토리에 저장
    public void save(JobPost jobPost) {
        JobPostForm form = new JobPostForm();
        // 1. form 데이터를 jobPost에 세팅 (생략)
        
        // 2. 선택된 Skill ID들로 엔티티 조회 후 연결
        List<Skill> selectedSkills = skillRepository.findAllById(form.getSkills());
        jobPost.setSkills(selectedSkills);

        jobPostRepository.save(jobPost);
    }


    // 4. 채용 공고 삭제 (데이터 무결성 유지)
    // - 해당 공고를 참조하는 Article과 StudyProgress에서 '참조를 null로 끊음' (고아 객체 방지)
    // - 연관 관계가 정리된 후 공고 삭제
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


    // 5-1. 채용 공고 전체 조회
    @Transactional(readOnly = true)
    public List<JobPost> getAllJobPosts() {
        return jobPostRepository.findAll();
    }

    // 5-2. 상세 조회 (ReadOnly)
    // - 특정 ID의 공고를 가져오며, DB 부하를 줄이기 위해 읽기 전용으로 처리
    @Transactional(readOnly = true)
    public JobPost findById(Long jobId) {
        return jobPostRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("해당 구인 공고를 찾을 수 없습니다. id=" + jobId));
    }

    // 5-3. 페이징 기반 공고 목록 조회
    // - 페이지 번호와 크기에 맞춰 공고를 가져옴 (게시판 목록용)
    public Page<JobPost> getJobPostList(Pageable pageable) {
        return jobPostRepository.findAll(pageable);
    }
    

    // 5-4. 대시보드용 공고 혼합 조회
    // - 마감 임박 공고 5개를 우선 보여주고, 모자라면 최신 공고로 채움
    @Transactional(readOnly = true)
    public List<JobPost> getJobPostsForDashboard() {
        Pageable top5 = PageRequest.of(0, 5);
        List<JobPost> jobs = jobPostRepository.findTop5ByDeadlineAfterOrderByDeadlineAsc(top5);
        
        // 만약 마감 임박 공고가 5개 미만이라면 최신 공고로 채움
        if (jobs.size() < 5) {
            int countToFill = 5 - jobs.size();
            List<JobPost> latestJobs = jobPostRepository.findTop5ByOrderByCreatedAtDesc(PageRequest.of(0, countToFill));
            jobs.addAll(latestJobs);
        }
        return jobs;
    }

    // 5-5. 공고 상세 DTO 변환 조회
    // - 엔티티를 외부 노출용 DTO로 변환하여 반환
    public JobPostDto getJobPostById(Long jobId) {
        // 1. repository에서 해당 id의 공고를 찾고, 없으면 예외 발생
        JobPost jobPost = jobPostRepository.findById(jobId)
                        .orElseThrow(() -> new IllegalArgumentException("해당 공고가 존재하지 않습니다. ID: " + jobId));
        // 2. 조회된 엔티티를 DTO로 변환하여 반환
        return convertToDto(jobPost);
    }


    // 6. 엔티티 -> DTO 변환 (내부 유틸리티)
    // - Skill 엔티티 리스트에서 ID만 추출하여 DTO에 담는 과정을 수행
    private JobPostDto convertToDto(JobPost jobPost) {
       
        // 1. 엔티티 객체 리스트(job.getSkills())에서 ID들만 추출하여 List<Long>으로 변환
        List<Long> skillIds = jobPost.getSkills().stream()
                                    .map(Skill::getId)
                                    .toList();

        return JobPostDto.builder()
                .jobId(jobPost.getJobId())
                .jobPostTitle(jobPost.getJobPostTitle())
                .companyName(jobPost.getCompanyName())
                .position(jobPost.getPosition())
                .description(jobPost.getDescription())
                .url(jobPost.getUrl())
                .deadline(jobPost.getDeadline())
                .skills(skillIds) // 엔티티에서 추출한 ID 리스트 사용
                .build();
    }

    
    // 7. 관심 공고 추가
    // - 특정 유저가 특정 공고에 '관심'을 표시할 때 InterestJob 테이블에 저장
    public void addInterestJob(Long userId, Long jobPostId) {
        JobPost jobPost = jobPostRepository.findById(jobPostId).orElseThrow();
        InterestJob interest = new InterestJob();
        interest.setUserId(userId);
        interest.setJobPost(jobPost);
        interestJobRepository.save(interest);
    }


    // 8. D-DAY 계산 유틸리티
    // - 오늘 날짜와 마감일 사이의 일수 차이를 계산
    public long calculateDDay(LocalDate deadline) {
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }
}