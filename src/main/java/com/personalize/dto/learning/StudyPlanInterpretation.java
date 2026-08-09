package com.personalize.dto.learning;

import lombok.Data;
import java.util.List;

@Data
public class StudyPlanInterpretation {
    private String title;
    private String description;
    private List<StudyPlanItemDto> items;

    @Data
    public static class StudyPlanItemDto {
        private String topic;
        private String content;
        private Integer estimatedMinutes;
        private String priority; // HIGH, MEDIUM, LOW
    }
}
