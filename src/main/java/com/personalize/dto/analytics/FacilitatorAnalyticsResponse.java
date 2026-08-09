package com.personalize.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FacilitatorAnalyticsResponse {
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private List<TopicPerformanceDto> topicPerformance;
    private Integer numberOfStudents;
    private List<DifficultQuestionDto> difficultQuestions;
    private List<CommonLearningGapDto> commonLearningGaps;
}
