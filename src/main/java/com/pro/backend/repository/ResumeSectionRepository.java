package com.pro.backend.repository;

import com.pro.backend.entity.ResumeSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeSectionRepository extends JpaRepository<ResumeSection, Integer> {
    List<ResumeSection> findByIsVisibleTrueOrderBySectionAscSortOrderAsc();
}

