package com.pro.backend.controller;

import com.pro.backend.constants.UrlConstants;
import com.pro.backend.entity.ResumeSection;
import com.pro.backend.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(UrlConstants.Resume.BASE)
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    // GET /api/resume  — public
    @GetMapping
    public ResponseEntity<List<ResumeSection>> getResume() {
        log.debug("GET /resume (public)");
        List<ResumeSection> sections = resumeService.getVisibleSections();
        log.debug("GET /resume — returned {} visible sections", sections.size());
        return ResponseEntity.ok(sections);
    }

    // GET /api/resume/all  — admin only (includes hidden sections)
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ResumeSection>> getAllSections() {
        log.debug("GET /resume/all (admin)");
        List<ResumeSection> sections = resumeService.getAllSections();
        log.debug("GET /resume/all — returned {} total sections", sections.size());
        return ResponseEntity.ok(sections);
    }

    // PUT /api/resume/{id}  — admin only
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResumeSection> update(@PathVariable Integer id,
                                                 @RequestBody ResumeSection updated) {
        log.info("PUT /resume/{} — title='{}' visible={}", id, updated.getTitle(), updated.isVisible());
        try {
            ResumeSection saved = resumeService.update(id, updated);
            log.info("PUT /resume/{} — saved successfully", id);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("PUT /resume/{} — FAILED: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // POST /api/resume  — admin only, add new section
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResumeSection> create(@RequestBody ResumeSection section) {
        log.info("POST /resume — section={} title='{}'", section.getSection(), section.getTitle());
        try {
            ResumeSection saved = resumeService.create(section);
            log.info("POST /resume — created id={}", saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("POST /resume — FAILED: {}", e.getMessage(), e);
            throw e;
        }
    }

    // DELETE /api/resume/{id}  — admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.info("DELETE /resume/{}", id);
        resumeService.delete(id);
        log.info("DELETE /resume/{} — deleted", id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/resume/reorder  — admin only
    // body: [ { "id": 1, "sortOrder": 0 }, ... ]
    @PatchMapping("/reorder")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> reorder(@RequestBody List<Map<String, Integer>> orders) {
        log.info("PATCH /resume/reorder — {} entries", orders.size());
        try {
            resumeService.reorder(orders);
            log.info("PATCH /resume/reorder — done");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("PATCH /resume/reorder — FAILED: {}", e.getMessage(), e);
            throw e;
        }
    }
}
