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
        return ResponseEntity.ok(postService.getPublishedPosts(pageable));
    }

    // GET /api/posts/{slug}
    @GetMapping(UrlConstants.Posts.BY_SLUG)
    public ResponseEntity<PostResponse> getPost(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getPostBySlug(slug));
    }

    // GET /api/posts/{postId}/comments
    @GetMapping(UrlConstants.Posts.COMMENTS)
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID postId) {
        return ResponseEntity.ok(commentService.getCommentsForPost(postId));
    }

    // POST /api/posts/{postId}/comments  — authenticated users only
    @PostMapping(UrlConstants.Posts.COMMENTS)
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        CommentResponse response = commentService.addComment(postId, currentUser.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
