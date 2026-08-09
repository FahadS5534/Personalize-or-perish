package com.personalize.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_gaps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningGap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity; // LOW, MEDIUM, HIGH

    @Column(nullable = false)
    private Double accuracy; // Percentage accuracy on this topic

    @Column(columnDefinition = "TEXT")
    private String explanation; // Qualitative explanation from Gemini

    @Column(name = "identified_at", nullable = false, updatable = false)
    private LocalDateTime identifiedAt;

    @PrePersist
    protected void onCreate() {
        this.identifiedAt = LocalDateTime.now();
    }
}
