package com.pro.backend.repository;

import com.pro.backend.entity.ResumeSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResumeSectionRepository extends JpaRepository<ResumeSection, Integer> {

    @Query("SELECT r FROM ResumeSection r WHERE r.visible = true ORDER BY r.section ASC, r.sortOrder ASC")
    List<ResumeSection> findVisibleOrderBySectionAndSortOrder();

    @Query("SELECT r FROM ResumeSection r ORDER BY r.section ASC, r.sortOrder ASC")
    List<ResumeSection> findAllOrderBySectionAndSortOrder();
}
