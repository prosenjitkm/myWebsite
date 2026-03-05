package com.pro.backend.repository;

import com.pro.backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findByIsPublishedTrueOrderByPublishedAtDesc(Pageable pageable);
    Optional<Post> findBySlug(String slug);
    boolean existsBySlug(String slug);
}

