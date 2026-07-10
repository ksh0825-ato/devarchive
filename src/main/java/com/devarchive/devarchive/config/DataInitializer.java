package com.devarchive.devarchive.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.Article;
import com.devarchive.devarchive.domain.ArticleTag;
import com.devarchive.devarchive.domain.InterestJob;
import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.domain.Skill;
import com.devarchive.devarchive.domain.StudyProgress;
import com.devarchive.devarchive.domain.StudyProgress.ProgressStatus;
import com.devarchive.devarchive.domain.Tag;
import com.devarchive.devarchive.domain.Visibility;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.repository.ArticleRepository;
import com.devarchive.devarchive.repository.ArticleTagRepository;
import com.devarchive.devarchive.repository.InterestJobRepository;
import com.devarchive.devarchive.repository.JobPostRepository;
import com.devarchive.devarchive.repository.SkillRepository;
import com.devarchive.devarchive.repository.StudyProgressRepository;
import com.devarchive.devarchive.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JobPostRepository jobPostRepository;
    private final ArticleRepository articleRepository;
    private final SkillRepository skillRepository;
    private final TagRepository tagRepository;
    private final InterestJobRepository interestJobRepository;
    private final ArticleTagRepository articleTagRepository;
    private final StudyProgressRepository studyProgressRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. 계정 생성
        if (accountRepository.findByUsername("user1").isEmpty()) {
            Account user1 = new Account();
            user1.setUsername("user1");
            user1.setPassword(passwordEncoder.encode("12345678"));
            user1.setEmail("user1@test.com");
            user1.setNickname("테스터1");
            user1.setRole("ROLE_USER");
            accountRepository.save(user1);

            Account user2 = new Account();
            user2.setUsername("user2");
            user2.setPassword(passwordEncoder.encode("12345678"));
            user2.setEmail("user2@test.com");
            user2.setNickname("테스터2");
            user2.setRole("ROLE_USER");
            accountRepository.save(user2);

            Account company = new Account();
            company.setUsername("company1");
            company.setPassword(passwordEncoder.encode("12345678"));
            company.setEmail("company1@test.com");
            company.setNickname("삼성전자채용팀");
            company.setRole("ROLE_COMPANY");
            accountRepository.save(company);
        }

        Account user1 = accountRepository.findByUsername("user1").get();
        Account company1 = accountRepository.findByUsername("company1").get();


        // 2. 기술 스택(Skill) 생성
        if (skillRepository.count() == 0) {
            skillRepository.save(new Skill("Java"));
            skillRepository.save(new Skill("JavaScript"));
            skillRepository.save(new Skill("Spring"));
            skillRepository.save(new Skill("Python"));
            skillRepository.save(new Skill("MySQL"));
        }


        // 3. 채용 공고(JobPost) 및 스킬 연결 생성 (D-Day 테스트 케이스 포함)
        if (jobPostRepository.count() == 0) {
            Skill java = skillRepository.findByName("Java")
                .orElseThrow(() -> new RuntimeException("Java 스킬을 찾을 수 없습니다."));

            // 헬퍼 메서드 사용을 위한 리스트 선언
            java.util.List<Skill> skills = Collections.singletonList(java);

            // [D-Day 테스트 케이스 생성]
            // 1. 여유 있는 공고 (오늘 이후)
            jobPostRepository.save(JobPost.builder()
                    .account(company1).jobPostTitle("백엔드 개발자 채용(여유)").companyName("삼성전자")
                    .url("https://www.samsung.com")
                    .position("백엔드").description("대규모 트래픽 처리 서버 개발")
                    .deadline(LocalDate.now().plusMonths(1)).skills(skills).createdAt(LocalDateTime.now()).build());

            // 2. 오늘 마감 (D-Day)
            jobPostRepository.save(JobPost.builder()
                    .account(company1).jobPostTitle("데이터 엔지니어 채용(오늘 마감 공고)").companyName("카카오")
                    .url("https://www.kakaocorp.com")
                    .position("데이터 엔지니어").deadline(LocalDate.now()).skills(skills).createdAt(LocalDateTime.now()).build());

            // 3. 마감된 공고
            jobPostRepository.save(JobPost.builder()
                    .account(company1).jobPostTitle("프론트엔드 채용(마감된 공고)").companyName("네이버")
                    .url("https://www.naver.com")
                    .position("백엔드").deadline(LocalDate.now().minusDays(5)).skills(skills).createdAt(LocalDateTime.now()).build());

            // 방금 만든 공고 중 하나를 학습 기록에 연결하기 위해 조회
            JobPost latestJob1 = jobPostRepository.findByJobPostTitle("백엔드 개발자 채용(여유)").get(0);
            JobPost latestJob2 = jobPostRepository.findByJobPostTitle("데이터 엔지니어 채용(오늘 마감 공고)").get(0);


            // 4. 학습 기록(Article) 생성
            // 공개 글
            Article article1 = Article.builder()
                    .account(user1).jobPost(latestJob1).title("삼성전자 백엔드 공고 분석(공개)")
                    .content("대규모 트래픽 아키텍처 학습 필요.").viewCount(0L)
                    .visibility(Visibility.PUBLIC).createdAt(LocalDateTime.now()).build();
            articleRepository.save(article1);

            Tag springTag = tagRepository.save(new Tag("Spring"));

            ArticleTag at1 = new ArticleTag(article1, springTag);
            articleTagRepository.save(at1);

            // 공고를 만들고 나서 사용자와 연결된 '학습 진행 상태(StudyProgress)' 생성
            StudyProgress progress1 = StudyProgress.builder()
                    .account(user1)
                    .jobPost(latestJob1) // 위에서 생성한 job
                    .status(ProgressStatus.STUDYING) // 초기 상태를 학습 중으로 설정
                    .build();
            studyProgressRepository.save(progress1);

            
            // 비공개 글
            Article article2 = Article.builder()
                    .account(user1).jobPost(latestJob2).title("카카오 데이터 엔지니어 분석(비공개)")
                    .content("데이터 파이프라인 학습 중.").viewCount(0L)
                    .visibility(Visibility.PRIVATE).createdAt(LocalDateTime.now()).build();
            articleRepository.save(article2);

            Tag javaTag = tagRepository.save(new Tag("Java"));

            ArticleTag at2 = new ArticleTag(article2, javaTag);
            articleTagRepository.save(at2);

            StudyProgress progress2 = StudyProgress.builder()
                    .account(user1)
                    .jobPost(latestJob2) // 위에서 생성한 job
                    .status(ProgressStatus.STUDYING) // 초기 상태를 학습 중으로 설정
                    .build();
            studyProgressRepository.save(progress2);

            
            // 5. 관심 공고 생성
            if (!interestJobRepository.existsByUserIdAndJobPostJobId(user1.getUserId(), latestJob1.getJobId())) {
                interestJobRepository.save(new InterestJob(user1.getUserId(), latestJob1));
            }
        }
    }
 }