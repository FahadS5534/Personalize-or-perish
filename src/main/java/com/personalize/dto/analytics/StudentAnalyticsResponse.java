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
public class StudentAnalyticsResponse {
    private Double overallScore;
    private List<TopicPerformanceDto> topicPerformance;
    private List<String> strongTopics;
    private List<String> weakTopics;
    private List<RecentAssessmentDto> recentAssessments;
}
