package com.personalize.dto.learning;

import lombok.Data;
import java.util.List;

@Data
public class LearningGapInterpretation {
    private String summary;
    private List<PriorityTopic> priorityTopics;

    @Data
    public static class PriorityTopic {
        private String topic;
        private String priority; // HIGH, MEDIUM, LOW
        private String reason;
        private List<String> recommendedActions;
    }
}
