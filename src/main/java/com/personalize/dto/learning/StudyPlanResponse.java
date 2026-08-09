package com.personalize.dto.learning;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StudyPlanResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private List<StudyPlanItemResponse> items;
}
