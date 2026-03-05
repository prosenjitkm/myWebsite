package com.pro.backend.service;

import com.pro.backend.constants.ServiceConstants;
import com.pro.backend.dto.PostResponse;
import com.pro.backend.entity.Post;
import com.pro.backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public Page<PostResponse> getPublishedPosts(Pageable pageable) {
        Page<PostResponse> page = postRepository
                .findByIsPublishedTrueOrderByPublishedAtDesc(pageable)
                .map(this::toResponse);
        log.debug("getPublishedPosts() — page={} returned={} total={}", pageable.getPageNumber(), page.getNumberOfElements(), page.getTotalElements());
        return page;
    }

    public PostResponse getPostBySlug(String slug) {
        log.debug("getPostBySlug() — slug={}", slug);
        Post post = postRepository.findBySlug(slug)
                .filter(Post::isPublished)
                .orElseThrow(() -> {
                    log.warn("getPostBySlug() — not found or unpublished: slug={}", slug);
                    return new IllegalArgumentException(ServiceConstants.ERR_POST_NOT_FOUND + ": " + slug);
                });
        log.debug("getPostBySlug() — found id={} title='{}'", post.getId(), post.getTitle());
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
