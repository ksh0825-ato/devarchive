package com.devarchive.devarchive.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.domain.Skill;
import com.devarchive.devarchive.dto.jobpost.JobPostDto;
import com.devarchive.devarchive.dto.jobpost.JobPostForm;
import com.devarchive.devarchive.repository.AccountRepository;
import com.devarchive.devarchive.repository.InterestJobRepository;
import com.devarchive.devarchive.repository.JobPostRepository;
import com.devarchive.devarchive.repository.SkillRepository;
import com.devarchive.devarchive.service.AccountService;
import com.devarchive.devarchive.service.JobPostService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/job")
public class JobPostController {
    
    // 1. private final로 선언
    private final JobPostService jobPostService;
    private final JobPostRepository jobPostRepository;
    private final SkillRepository skillRepository;
    private final InterestJobRepository interestJobRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    // 공고 목록 조회
    @GetMapping("/JobPostList")
    public String jobPostList(Model model, Authentication auth, HttpServletRequest request,@PageableDefault(size = 10) Pageable pageable) {
        System.out.println("게시판 조회 진입 시도");
            Page<JobPost> jobPostList = jobPostRepository.findAll(pageable);
            // 1. 기본값 false
                boolean isCompany = false;
                
                // 2. 로그인되어 있고, 권한이 있는지 확인
                if (auth != null && auth.getAuthorities() != null) {
                    isCompany = auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY"));
                }

            model.addAttribute("job", jobPostList);
            model.addAttribute("maxPage", 5);
            // 오늘 날짜를 모델에 추가 (마감일 계산용)
            model.addAttribute("today", java.time.LocalDate.now()); 
            model.addAttribute("isCompany", isCompany);

            return "job/JobPostList";

    }

    // 공고 등록 페이지
    @GetMapping("/JobPostRegister")
    public String jobPostRegisterForm(Principal principal, HttpServletRequest request, Model model) { // void -> String으로 변경

        // 일반 회원 접근 차단 (공고 수정은 기업만 가능할 경우)
        if (!request.isUserInRole("ROLE_COMPANY")) {
            model.addAttribute("message", "채용 공고는 기업 회원만 작성할 수 있습니다.");
            model.addAttribute("redirectUrl", "/job/JobPostList"); // 목록으로 이동
            return "common/alert";
        }

        model.addAttribute("allSkills", skillRepository.findAll()); // 모든 스킬 목록 조회
        model.addAttribute("jobPostForm", new JobPostForm());
        return "job/JobPostRegister";
    }

    // 공고 등록 처리
    @PreAuthorize("hasRole('COMPANY')")
    @PostMapping("/JobPostRegister")
    public String jobPostRegister(@ModelAttribute JobPostDto jobPostDto, Model model, Principal principal, Pageable pageable) {
        Page<JobPost> jobPosts = jobPostService.getJobPostList(pageable);

        // 1. 데이터 확인을 위한 로그 (값이 잘 넘어오는지 확인)
        System.out.println("등록 시도 - 제목: " + jobPostDto.getJobPostTitle());

        model.addAttribute("today", LocalDate.now());
        model.addAttribute("job", jobPosts);
        
        // 2. 서비스 호출 (username을 principal.getName()으로 전달)
        // 이 부분이 빠져 있으면 아무것도 저장되지 않습니다.
        jobPostService.registerJobPost(jobPostDto, principal.getName());
        
        // 3. 완료 후 목록으로 이동
        return "redirect:/job/JobPostList";

    }

    // 게시글 상세 조회
    @GetMapping("/JobPostDetail")
    public String jobPostDetail(@RequestParam Long jobId, 
                                @AuthenticationPrincipal UserDetails userDetails, 
                                Model model) {
        
        // 1. 로그인 여부 확인 및 관심 여부 세팅
        boolean isInterested = false;
        
        if (userDetails != null) {
            // userDetails에서 직접 username을 가져옵니다.
            String username = userDetails.getUsername(); 
            Account account = accountRepository.findByUsername(username)
                    .orElse(null);
            
            if (account != null) {
                isInterested = interestJobRepository.existsByUserIdAndJobPostJobId(
                        account.getUserId(),
                        jobId
                );
            } else {
                System.out.println("account = null");
            }
            
        } else {
            System.out.println("userDetails = null");
        }

        // 2. 게시글 상세 정보 조회
        JobPost job = jobPostService.findById(jobId);
        model.addAttribute("job", job);
        model.addAttribute("isInterested", isInterested);
        model.addAttribute("today", java.time.LocalDate.now());
        
        // 권한 확인 (필요시)
        boolean isCompany = (userDetails != null && userDetails.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY")));
        model.addAttribute("isCompany", isCompany);
        
        return "job/JobPostDetail";
    }

    // 채용 공고 수정 페이지 이동
    @GetMapping("/JobPostUpdate")
    public String updateForm(@RequestParam Long jobId, Principal principal, HttpServletRequest request, Model model) {
        JobPost job = jobPostService.findById(jobId);
        
        JobPostForm form = new JobPostForm();
        form.setJobPostTitle(job.getJobPostTitle());

        // 일반 회원 접근 차단 (공고 수정은 기업만 가능할 경우)
        if (!request.isUserInRole("ROLE_COMPANY")) {
            model.addAttribute("message", "채용 공고는 기업 회원만 수정할 수 있습니다.");
            model.addAttribute("redirectUrl", "/job/JobPostList"); // 목록으로 이동
            return "common/alert";
        }

        // 2. 현재 공고에 설정된 스킬 ID 리스트를 form에 넣음
        List<Long> skillIds = job.getSkills().stream().map(Skill::getId).toList();
        form.setSkills(skillIds);
        
        model.addAttribute("jobPostForm", form);
        model.addAttribute("allSkills", skillRepository.findAll()); // 전체 스킬 리스트
        model.addAttribute("job", job); // 업데이트 요청 시 사용할 ID

        return "job/JobPostUpdate";
    }

    // 실제 수정 처리
    @PreAuthorize("hasRole('COMPANY')")
    @PostMapping("/JobPostUpdate")
    public String update(@RequestParam("jobId") Long jobId, JobPostForm form, JobPost job) {
        // DTO 없이 바로 Form만 서비스로 넘겨 처리합니다.
            jobPostService.updateJobPost(jobId, form, job);
            return "redirect:/job/JobPostDetail?jobId=" + jobId;
    }


    // 채용 공고 삭제 처리
    @PostMapping("/job/JobPostDelete")
    public String delete(@RequestParam("jobId") Long jobId, Principal principal, HttpServletRequest request, Model model) {

        jobPostService.deleteJobPost(jobId);
        return "redirect:/job/JobPostList";
    }

}