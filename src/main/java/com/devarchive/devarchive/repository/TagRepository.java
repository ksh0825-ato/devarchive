package com.devarchive.devarchive.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devarchive.devarchive.domain.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTagName(String tagName);
}