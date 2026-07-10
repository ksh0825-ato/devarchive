package com.devarchive.devarchive.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import com.devarchive.devarchive.service.InterestJobService;
import com.devarchive.devarchive.service.JobPostService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/job")
public class JobPostController {

    private final JobPostService jobPostService;
    private final JobPostRepository jobPostRepository;
    private final SkillRepository skillRepository;
    private final InterestJobRepository interestJobRepository;
    private final AccountRepository accountRepository;
    private final InterestJobService interestJobService;

    // 1. 공고 목록 조회(get)
    @GetMapping("/JobPostList")
    public String jobPostList(Model model, Authentication auth, @PageableDefault(size = 10) Pageable pageable) {
        
        // 1. 공고 목록 조회
        Page<JobPost> jobPostList = jobPostRepository.findAll(pageable);
        
        boolean isCompany = false;
        Set<Long> interestedJobIds = new HashSet<>(); // 관심 공고 ID 담을 곳

        // 2. 로그인 여부 확인 및 권한/관심 목록 조회
        if (auth != null && auth.isAuthenticated()) {
            // 권한 체크
            isCompany = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY"));
            
            // 유저 정보 가져오기 (UserDetails 기반)
            if (!isCompany) { // 기업 회원이 아닐 경우에만
                UserDetails userDetails = (UserDetails) auth.getPrincipal();
                Account account = accountRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
                
                // 유저 ID로 관심 공고들의 ID 리스트만 뽑아냄
                interestedJobIds = interestJobService.findAllByUserId(account.getUserId())
                        .stream()
                        .map(ij -> ij.getJobPost().getJobId())
                        .collect(Collectors.toSet());
            }
        }

        model.addAttribute("job", jobPostList);
        model.addAttribute("isCompany", isCompany);
        model.addAttribute("today", java.time.LocalDate.now());
        model.addAttribute("interestedJobIds", interestedJobIds); // 찜 목록 모델에 추가

        return "job/JobPostList";
    }


    // 2-1. 공고 등록 페이지(get)
    @GetMapping("/JobPostRegister")
    public String jobPostRegisterForm(Principal principal, HttpServletRequest request,
                                      Model model) { // void -> String으로 변경

        // 일반 회원 접근 차단
        if (!request.isUserInRole("ROLE_COMPANY")) {
            model.addAttribute("message", "채용 공고는 기업 회원만 작성할 수 있습니다.");
            model.addAttribute("redirectUrl", "/job/JobPostList");
            return "common/alert";
        }

        model.addAttribute("allSkills", skillRepository.findAll());
        model.addAttribute("jobPostForm", new JobPostForm());

        return "job/JobPostRegister";
    }

    // 2-2. 공고 등록 처리(post)
    @PreAuthorize("hasRole('COMPANY')")
    @PostMapping("/JobPostRegister")
    public String jobPostRegister(@ModelAttribute JobPostDto jobPostDto, Model model,
                                  BindingResult bindingResult, Principal principal, Pageable pageable) {
        
        // 0-1. 예외 처리
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return alert(model, msg, "back");
        }

        // 0-2. 날짜 비교 로직
        if (jobPostDto.getDeadline() != null && jobPostDto.getDeadline().isBefore(LocalDate.now())) {
            return alert(model, "마감일은 오늘 이후로 설정해야 합니다.", "back");
        }

        Page<JobPost> jobPosts = jobPostService.getJobPostList(pageable);
        
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("job", jobPosts);
        
        // 서비스 호출 (username을 principal.getName()으로 전달)
        jobPostService.registerJobPost(jobPostDto, principal.getName());
        
        return "redirect:/job/JobPostList";
    }


    // 3. 게시글 상세 조회(get)
    @GetMapping("/JobPostDetail")
    public String jobPostDetail(@RequestParam Long jobId, 
                                @AuthenticationPrincipal UserDetails userDetails, 
                                Model model) {
        
        // 1-1. 로그인 여부 확인 및 관심 여부 세팅
        boolean isInterested = false;
        
        if (userDetails != null) {
            Optional<Account> accountOpt = accountRepository.findByUsername(userDetails.getUsername());
            
            if (accountOpt.isPresent()) {
                // 직접 변수에 값을 대입 (오류 없음)
                isInterested = interestJobRepository.existsByUserIdAndJobPostJobId(
                    accountOpt.get().getUserId(), 
                    jobId
                );
            }
        }

        // 1-2. 권한 확인
        boolean isCompany = (userDetails != null && userDetails.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY")));
        model.addAttribute("isCompany", isCompany);


        // 2. 게시글 상세 정보 조회
        JobPost job = jobPostService.findById(jobId);
        model.addAttribute("job", job);
        model.addAttribute("isInterested", isInterested);
        model.addAttribute("today", java.time.LocalDate.now());
        
        return "job/JobPostDetail";
    }

    // 4-1. 수정 페이지 이동(get)
    @GetMapping("/JobPostUpdate")
    public String updateForm(@RequestParam Long jobId, Principal principal,
                            HttpServletRequest request, Model model) {

        JobPost job = jobPostService.findById(jobId);
        
        JobPostForm form = new JobPostForm();
        form.setJobPostTitle(job.getJobPostTitle());

        // 0. 일반 회원 접근 차단
        if (!request.isUserInRole("ROLE_COMPANY")) {
            model.addAttribute("message", "채용 공고는 기업 회원만 수정할 수 있습니다.");
            model.addAttribute("redirectUrl", "/job/JobPostList");
            return "common/alert";
        }

        // 1. 현재 공고에 설정된 스킬 ID 리스트를 form에 넣음
        List<Long> skillIds = job.getSkills().stream().map(Skill::getId).toList();
        form.setSkills(skillIds);
        
        model.addAttribute("jobPostForm", form);
        model.addAttribute("allSkills", skillRepository.findAll()); // 전체 스킬 리스트
        model.addAttribute("job", job); // 업데이트 요청 시 사용할 ID

        return "job/JobPostUpdate";
    }

    // 4-2. 실제 수정 처리
    @PreAuthorize("hasRole('COMPANY')")
    @PostMapping("/JobPostUpdate")
    public String update(@Valid @RequestParam("jobId") Long jobId, JobPostDto jobPostDto,
                         JobPostForm form, JobPost job,
                         BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return alert(model, msg, "back");
        }

        // 날짜 비교 로직
        if (jobPostDto.getDeadline() != null && jobPostDto.getDeadline().isBefore(LocalDate.now())) {
            return alert(model, "마감일은 오늘 이후로 설정해야 합니다.", "back");
        }

        jobPostService.updateJobPost(jobId, form, job);

        return "redirect:/job/JobPostDetail?jobId=" + jobId;
    }

    
    // 5-1. 채용 공고 삭제(get)(실수로 GET으로 접근할 경우를 대비한 방어 코드)
    @GetMapping("/JobPostDelete")
    public String deleteGet() {
        return "redirect:/job/JobPostList";
    }

    // 5-2. 채용 공고 삭제 처리
    @PostMapping("/JobPostDelete")
    public String delete(@RequestParam("jobId") Long jobId, Principal principal) {

        jobPostService.deleteJobPost(jobId);

        return "redirect:/job/JobPostList";
    }


    // alert용
    private String alert(Model model, String message, String redirectUrl) {
        model.addAttribute("message", message);
        model.addAttribute("redirectUrl", redirectUrl);
        return "common/alert";
    }

}