package com.devarchive.devarchive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devarchive.devarchive.domain.Skill;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    // 필요한 경우 추가적인 쿼리 메서드 정의 가능
}
