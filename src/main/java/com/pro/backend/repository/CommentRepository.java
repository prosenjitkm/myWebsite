package com.pro.backend.repository;

import com.pro.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    // top-level approved comments for a post, oldest first
    List<Comment> findByPostIdAndParentIsNullAndIsApprovedTrueOrderByCreatedAtAsc(UUID postId);
    // replies to a specific comment
    List<Comment> findByParentIdAndIsApprovedTrueOrderByCreatedAtAsc(UUID parentId);
}

