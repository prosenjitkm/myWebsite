package com.pro.backend.service;

import com.pro.backend.constants.ServiceConstants;
import com.pro.backend.dto.CommentRequest;
import com.pro.backend.dto.CommentResponse;
import com.pro.backend.entity.Comment;
import com.pro.backend.entity.Post;
import com.pro.backend.entity.User;
import com.pro.backend.repository.CommentRepository;
import com.pro.backend.repository.PostRepository;
import com.pro.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsForPost(UUID postId) {
        return commentRepository
                .findByPostIdAndParentIsNullAndIsApprovedTrueOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse addComment(UUID postId, String authorEmail, CommentRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(ServiceConstants.ERR_POST_NOT_FOUND));

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException(ServiceConstants.ERR_USER_NOT_FOUND));

        Comment parent = null;
        if (req.parentId() != null) {
            parent = commentRepository.findById(req.parentId())
                    .orElseThrow(() -> new IllegalArgumentException(ServiceConstants.ERR_COMMENT_NOT_FOUND));
        }

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .parent(parent)
                .body(req.body())
                .isApproved(true)
                .build();

        return toResponse(commentRepository.save(comment));
    }

    private CommentResponse toResponse(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getBody(),
                c.getAuthor().getUsername(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getCreatedAt()
        );
    }
}

