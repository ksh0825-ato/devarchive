package com.devarchive.devarchive.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devarchive.devarchive.domain.Account;
import com.devarchive.devarchive.domain.InterestJob;

public interface InterestJobRepository extends JpaRepository<InterestJob, Long> {
    // 특정 회원의 관심 공고 목록 조회
    List<InterestJob> findByAccount(Account account);
}