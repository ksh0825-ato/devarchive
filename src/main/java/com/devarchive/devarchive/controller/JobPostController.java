package com.devarchive.devarchive.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.devarchive.devarchive.domain.JobPost;
import com.devarchive.devarchive.dto.jobpost.JobPostDto;
import com.devarchive.devarchive.service.JobPostService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/job")
public class JobPostController {
    // 1. private final로 선언
    private final JobPostService jobPostService;

    // 공고 목록 조회
    @GetMapping("/JobPostList")
    public String jobPostList(Model model, HttpServletRequest request,@PageableDefault(size = 10) Pageable pageable) {
        System.out.println("게시판 조회 진입 시도");

        // 세션 검증 로직 제거! 
        // Spring Security가 설정된 권한(SecurityConfig)에 따라 
        // 인증되지 않은 사용자는 아예 이 메서드에 들어오지 못하게 차단합니다.
    
        Page<JobPost> jobPostList = jobPostService.getJobPostList(pageable);

        model.addAttribute("job", jobPostList);
        model.addAttribute("maxPage", 5);

        return "job/JobPostList";

    }

    // 공고 등록 페이지
    @GetMapping("/JobPostRegister")
    public void jobPostRegisterForm(Model model) {
      
    }

    // 공고 등록 처리
    // 게시글 등록 처리
    @PreAuthorize("hasRole('COMPANY')")
    @PostMapping("/JobPostRegister")
    public String jobPostRegister(@ModelAttribute JobPostDto jobPostDto, Principal principal) {

        // 1. 데이터 확인을 위한 로그 (값이 잘 넘어오는지 확인)
        System.out.println("등록 시도 - 제목: " + jobPostDto.getJobPostTitle());
        
        // 2. 서비스 호출 (username을 principal.getName()으로 전달)
        // 이 부분이 빠져 있으면 아무것도 저장되지 않습니다.
        jobPostService.registerJobPost(jobPostDto, principal.getName());
        
        // 3. 완료 후 목록으로 이동
        return "redirect:/job/JobPostList";

    }


    // 게시글 상세 조회
    @GetMapping("/JobPostDetail")
        public String jobPostDetail(@RequestParam("jobId") Long jobId, Model model) {
        // 서비스에서 상세 데이터 가져오기
        JobPostDto jobPostDto = jobPostService.getJobPostById(jobId);
        
        // 모델에 담아서 화면으로 전달
        model.addAttribute("job", jobPostDto);
        
        return "job/JobPostDetail"; // 상세 페이지 HTML 경로
    }

    // 채용 공고 수정 페이지 이동
    @GetMapping("/JobPostUpdate")
    public String updateForm(@RequestParam("jobId") Long jobId, Model model) {
        model.addAttribute("job", jobPostService.getJobPostById(jobId));
        return "job/JobPostUpdate"; // 수정용 HTML 생성 필요
    }

    // 실제 수정 처리
    @PreAuthorize("hasRole('COMPANY')")
    @PostMapping("/JobPostUpdate")
    public String update(@RequestParam("jobId") Long jobId, JobPostDto dto) {
        System.out.println("수정 요청 확인 - ID: " + jobId);
        System.out.println("받아온 제목: " + dto.getJobPostTitle());
        
        jobPostService.updateJobPost(jobId, dto);
        return "redirect:/job/JobPostDetail?jobId=" + jobId; // 수정 후 상세페이지로
    }


    // 채용 공고 삭제 처리
    @PostMapping("/job/JobPostDelete")
    public String delete(@RequestParam("jobId") Long jobId) {
        jobPostService.deleteJobPost(jobId);
        return "redirect:/job/JobPostList";
    }

}