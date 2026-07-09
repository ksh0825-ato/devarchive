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
import com.devarchive.devarchive.domain.Tag;
import com.devarchive.devarchive.domain.Visibility;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.repository.ArticleRepository;
import com.devarchive.devarchive.repository.InterestJobRepository;
import com.devarchive.devarchive.repository.JobPostRepository;
import com.devarchive.devarchive.repository.SkillRepository;
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

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. 계정 생성
        if (accountRepository.findByUsername("user1").isEmpty()) {
            Account user = new Account();
            user.setUsername("user1");
            user.setPassword(passwordEncoder.encode("1234"));
            user.setNickname("테스터1");
            user.setRole("ROLE_USER");
            accountRepository.save(user);

            Account company = new Account();
            company.setUsername("company1");
            company.setPassword(passwordEncoder.encode("1234"));
            company.setNickname("삼성전자채용팀");
            company.setRole("ROLE_COMPANY");
            accountRepository.save(company);
        }

        Account user = accountRepository.findByUsername("user1").get();

        // 2. 기술 스택(Skill) 생성
        if (skillRepository.count() == 0) {
            skillRepository.save(new Skill("Java"));
            skillRepository.save(new Skill("JavaScript"));
            skillRepository.save(new Skill("Spring"));
            skillRepository.save(new Skill("Python"));
            skillRepository.save(new Skill("MySQL"));
        }

        // 3. 채용 공고(JobPost) 및 스킬 연결 생성
        if (jobPostRepository.count() == 0) {
            Skill java = skillRepository.findByName("Java")
                .orElseThrow(() -> new RuntimeException("Java 스킬을 찾을 수 없습니다."));
            
            JobPost job = JobPost.builder()
                    .account(user)
                    .jobPostTitle("백엔드 개발자 채용")
                    .companyName("삼성전자")
                    .position("백엔드")
                    .description("대규모 트래픽 처리를 위한 서버 개발")
                    .deadline(LocalDate.now().plusMonths(1))
                    .skills(Collections.singletonList(java))
                    .createdAt(LocalDateTime.now())
                    .build();
            jobPostRepository.save(job);

            // 4. 학습 기록(Article) 및 태그(Tag) 연결 생성
            Tag springTag = tagRepository.save(new Tag("Spring"));
            Tag javaTag = tagRepository.save(new Tag("Java"));

            Article article1 = Article.builder()
                    .account(user)
                    .jobPost(job)
                    .title("삼성전자 백엔드 공고 분석(비공개)")
                    .content("대규모 트래픽 아키텍처 학습 필요.")
                    .viewCount(0L)
                    .visibility(Visibility.PRIVATE) // 비공개 설정
                    .createdAt(LocalDateTime.now())
                    .build();
            articleRepository.save(article1);

            // 공개 여부 테스트용
            Article article2 = Article.builder()
                    .account(user)
                    .jobPost(job)
                    .title("삼성전자 백엔드 공고 분석(공개)")
                    .content("대규모 트래픽 아키텍처 학습 필요.")
                    .viewCount(0L)
                    .visibility(Visibility.PUBLIC) // 공개 설정!
                    .createdAt(LocalDateTime.now())
                    .build();
            articleRepository.save(article2);

            // ArticleTag 직접 생성
            ArticleTag articleTag1 = new ArticleTag(article1, springTag);
            ArticleTag articleTag2 = new ArticleTag(article2, javaTag);
            // 만약 Article 엔티티에 list가 있다면 article.getArticleTags().add(articleTag) 필요
            
            // 5. 관심 공고(InterestJob) 생성
            if (!interestJobRepository.existsByUserIdAndJobPostJobId(user.getUserId(), job.getJobId())) {
                interestJobRepository.save(new InterestJob(user.getUserId(), job));
            }
        }
    }
}