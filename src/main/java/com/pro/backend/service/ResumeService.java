package com.pro.backend.service;

import com.pro.backend.entity.ResumeSection;
import com.pro.backend.repository.ResumeSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeSectionRepository resumeSectionRepository;

    public List<ResumeSection> getVisibleSections() {
        return resumeSectionRepository.findByIsVisibleTrueOrderBySectionAscSortOrderAsc();
    }

    public List<ResumeSection> getAllSections() {
        return resumeSectionRepository.findAllByOrderBySectionAscSortOrderAsc();
    }

    @Transactional
    public ResumeSection update(Integer id, ResumeSection updated) {
        ResumeSection existing = resumeSectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resume section not found: " + id));
        existing.setSection(updated.getSection());
        existing.setSortOrder(updated.getSortOrder());
        existing.setTitle(updated.getTitle());
        existing.setSubtitle(updated.getSubtitle());
        existing.setLocation(updated.getLocation());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setDescription(updated.getDescription());
        existing.setVisible(updated.isVisible());
        existing.setUpdatedAt(OffsetDateTime.now());
        return resumeSectionRepository.save(existing);
    }

    @Transactional
    public ResumeSection create(ResumeSection section) {
        return resumeSectionRepository.save(section);
    }

    @Transactional
    public void delete(Integer id) {
        resumeSectionRepository.deleteById(id);
    }
}
