package com.pro.backend.controller;

import com.pro.backend.constants.UrlConstants;
import com.pro.backend.entity.ResumeSection;
import com.pro.backend.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(UrlConstants.Resume.BASE)
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    // GET /api/resume
    @GetMapping
    public ResponseEntity<List<ResumeSection>> getResume() {
        return ResponseEntity.ok(resumeService.getVisibleSections());
    }
}
