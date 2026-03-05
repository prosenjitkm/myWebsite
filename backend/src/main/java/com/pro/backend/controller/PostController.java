package com.pro.backend.controller;

import com.pro.backend.constants.ServiceConstants;
import com.pro.backend.constants.UrlConstants;
import com.pro.backend.dto.CommentRequest;
import com.pro.backend.dto.CommentResponse;
import com.pro.backend.dto.PostResponse;
import com.pro.backend.service.CommentService;
import com.pro.backend.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(UrlConstants.Posts.BASE)
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    // GET /api/posts?page=0&size=10
    @GetMapping
    public ResponseEntity<Page<PostResponse>> listPosts(
            @PageableDefault(size = ServiceConstants.DEFAULT_PAGE_SIZE,
                             sort = ServiceConstants.DEFAULT_POST_SORT) Pageable pageable) {
        log.debug("GET /posts — page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<PostResponse> page = postService.getPublishedPosts(pageable);
        log.debug("GET /posts — returned {} posts", page.getTotalElements());
        return ResponseEntity.ok(page);
    }

    // GET /api/posts/{slug}
    @GetMapping(UrlConstants.Posts.BY_SLUG)
    public ResponseEntity<PostResponse> getPost(@PathVariable String slug) {
        log.info("GET /posts/{}", slug);
        try {
            PostResponse post = postService.getPostBySlug(slug);
            log.debug("GET /posts/{} — found title='{}'", slug, post.title());
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            log.warn("GET /posts/{} — not found: {}", slug, e.getMessage());
            throw e;
        }
    }

    // GET /api/posts/{postId}/comments
    @GetMapping(UrlConstants.Posts.COMMENTS)
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID postId) {
        log.debug("GET /posts/{}/comments", postId);
        List<CommentResponse> comments = commentService.getCommentsForPost(postId);
        log.debug("GET /posts/{}/comments — returned {} comments", postId, comments.size());
        return ResponseEntity.ok(comments);
    }

    // POST /api/posts/{postId}/comments  — authenticated users only
    @PostMapping(UrlConstants.Posts.COMMENTS)
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        log.info("POST /posts/{}/comments — user={}", postId, currentUser.getUsername());
        try {
            CommentResponse response = commentService.addComment(postId, currentUser.getUsername(), request);
            log.info("Comment added — postId={} commentId={}", postId, response.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Add comment failed — postId={} user={} reason={}", postId, currentUser.getUsername(), e.getMessage());
            throw e;
        }
    }
}
