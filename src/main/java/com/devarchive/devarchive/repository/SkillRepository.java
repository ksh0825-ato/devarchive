package com.devarchive.devarchive.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devarchive.devarchive.domain.Skill;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
// 1. 이름으로 Skill을 찾는 메서드 추가
    // Optional을 사용하면 데이터가 없을 때 안전하게 처리할 수 있습니다.
    Optional<Skill> findByName(String name);
}
