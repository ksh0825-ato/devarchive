package com.devarchive.devarchive.config;

import com.devarchive.devarchive.domain.*;
import com.devarchive.devarchive.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

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
            skillRepository.save(new Skill("React"));
            skillRepository.save(new Skill("Spring"));
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
                    .build();
            jobPostRepository.save(job);

            // 4. 학습 기록(Article) 및 태그(Tag) 연결 생성
            Tag springTag = tagRepository.save(new Tag("Spring"));
            
            Article article = Article.builder()
                    .account(user)
                    .jobPost(job)
                    .title("삼성전자 백엔드 공고 분석")
                    .content("대규모 트래픽 아키텍처 학습 필요.")
                    .viewCount(0L)
                    .build();
            articleRepository.save(article);

            // ArticleTag 직접 생성
            ArticleTag articleTag = new ArticleTag(article, springTag);
            // 만약 Article 엔티티에 list가 있다면 article.getArticleTags().add(articleTag) 필요
            
            // 5. 관심 공고(InterestJob) 생성
            if (!interestJobRepository.existsByUserIdAndJobPostJobId(user.getUserId(), job.getJobId())) {
                interestJobRepository.save(new InterestJob(user.getUserId(), job));
            }
        }
    }
}