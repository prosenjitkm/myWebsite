package com.pro.backend.service;

import com.pro.backend.entity.ResumeSection;
import com.pro.backend.repository.ResumeSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeSectionRepository resumeSectionRepository;

    public List<ResumeSection> getVisibleSections() {
        return resumeSectionRepository.findByIsVisibleTrueOrderBySectionAscSortOrderAsc();
    }
}

