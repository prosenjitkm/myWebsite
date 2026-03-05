package com.pro.backend.controller;

import com.pro.backend.constants.UrlConstants;
import com.pro.backend.entity.ResumeSection;
import com.pro.backend.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(UrlConstants.Resume.BASE)
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    // GET /api/resume  — public
    @GetMapping
    public ResponseEntity<List<ResumeSection>> getResume() {
        return ResponseEntity.ok(resumeService.getVisibleSections());
    }

    // GET /api/resume/all  — admin only (includes hidden sections)
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ResumeSection>> getAllSections() {
        return ResponseEntity.ok(resumeService.getAllSections());
    }

    // PUT /api/resume/{id}  — admin only
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResumeSection> update(@PathVariable Integer id,
                                                 @RequestBody ResumeSection updated) {
        return ResponseEntity.ok(resumeService.update(id, updated));
    }

    // POST /api/resume  — admin only, add new section
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResumeSection> create(@RequestBody ResumeSection section) {
        return ResponseEntity.ok(resumeService.create(section));
    }

    // DELETE /api/resume/{id}  — admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        resumeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
