package com.pro.backend.service;

import com.pro.backend.entity.ResumeSection;
import com.pro.backend.repository.ResumeSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeSectionRepository resumeSectionRepository;

    public List<ResumeSection> getVisibleSections() {
        List<ResumeSection> sections = resumeSectionRepository.findVisibleOrderBySectionAndSortOrder();
        log.debug("getVisibleSections() — found {}", sections.size());
        return sections;
    }

    public List<ResumeSection> getAllSections() {
        List<ResumeSection> sections = resumeSectionRepository.findAllOrderBySectionAndSortOrder();
        log.debug("getAllSections() — found {}", sections.size());
        return sections;
    }

    @Transactional
    public ResumeSection update(Integer id, ResumeSection updated) {
        log.debug("update() — id={} title='{}' descLen={} visible={}",
                id, updated.getTitle(),
                updated.getDescription() == null ? 0 : updated.getDescription().length(),
                updated.isVisible());
        ResumeSection existing = resumeSectionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("update() — id={} NOT FOUND", id);
                    return new IllegalArgumentException("Resume section not found: " + id);
                });
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
        ResumeSection saved = resumeSectionRepository.save(existing);
        log.info("update() — id={} saved ok", id);
        return saved;
    }

    @Transactional
    public ResumeSection create(ResumeSection section) {
        log.debug("create() — section={} title='{}'", section.getSection(), section.getTitle());
        ResumeSection saved = resumeSectionRepository.save(section);
        log.info("create() — new id={} section={}", saved.getId(), saved.getSection());
        return saved;
    }

    @Transactional
    public void delete(Integer id) {
        log.info("delete() — id={}", id);
        resumeSectionRepository.deleteById(id);
        log.debug("delete() — id={} done", id);
    }

    @Transactional
    public void reorder(List<Map<String, Integer>> orders) {
        log.debug("reorder() — {} entries", orders.size());
        orders.forEach(entry -> {
            Integer id        = entry.get("id");
            Integer sortOrder = entry.get("sortOrder");
            resumeSectionRepository.findById(id).ifPresent(s -> {
                s.setSortOrder(sortOrder.shortValue());
                s.setUpdatedAt(OffsetDateTime.now());
                resumeSectionRepository.save(s);
                log.debug("reorder() — id={} sortOrder={}", id, sortOrder);
            });
        });
        log.info("reorder() — complete");
    }
}
