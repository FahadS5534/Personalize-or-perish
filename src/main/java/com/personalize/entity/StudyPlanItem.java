package com.personalize.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "study_plan_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_plan_id", nullable = false)
    private StudyPlan studyPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "estimated_minutes", nullable = false)
    private Integer estimatedMinutes;

    @Column(nullable = false)
    private String priority; // "HIGH", "MEDIUM", "LOW"

    @Column(nullable = false)
    private Boolean completed;
}
