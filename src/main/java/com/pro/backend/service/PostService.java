package com.pro.backend.service;

import com.pro.backend.constants.ServiceConstants;
import com.pro.backend.dto.PostResponse;
import com.pro.backend.entity.Post;
import com.pro.backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public Page<PostResponse> getPublishedPosts(Pageable pageable) {
        return postRepository
                .findByIsPublishedTrueOrderByPublishedAtDesc(pageable)
                .map(this::toResponse);
    }

    public PostResponse getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .filter(Post::isPublished)
                .orElseThrow(() -> new IllegalArgumentException(ServiceConstants.ERR_POST_NOT_FOUND + ": " + slug));
        return toResponse(post);
    }

    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getBody(),
                post.getCoverImage(),
                post.getTags().stream().map(t -> t.getName()).toList(),
                post.getAuthor().getUsername(),
                post.getPublishedAt()
        );
    }
}

