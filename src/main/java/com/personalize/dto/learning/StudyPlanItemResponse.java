package com.personalize.dto.learning;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudyPlanItemResponse {
    private Long id;
    private Long topicId;
    private String topicName;
    private String content;
    private Integer estimatedMinutes;
    private String priority;
    private Boolean completed;
}
