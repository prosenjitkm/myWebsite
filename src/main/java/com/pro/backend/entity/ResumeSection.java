package com.pro.backend.entity;

import com.pro.backend.constants.FieldConstants;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "resume_sections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResumeSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = FieldConstants.RESUME_SECTION_MAX)
    private String section;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Short sortOrder = 0;

    @Column(nullable = false, length = FieldConstants.RESUME_TITLE_MAX)
    private String title;

    @Column(length = FieldConstants.RESUME_SUBTITLE_MAX)
    private String subtitle;

    @Column(length = FieldConstants.RESUME_LOCATION_MAX)
    private String location;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private boolean isVisible = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() { createdAt = updatedAt = OffsetDateTime.now(); }

    @PreUpdate
    void preUpdate() { updatedAt = OffsetDateTime.now(); }
}
