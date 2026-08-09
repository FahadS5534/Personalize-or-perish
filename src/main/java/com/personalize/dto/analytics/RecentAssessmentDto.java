package com.personalize.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentAssessmentDto {
    private Long assessmentId;
    private String title;
    private Double score;
    private LocalDateTime submittedAt;
}
