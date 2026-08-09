package com.personalize.dto.assessment;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AssessmentResponse {
    private Long id;
    private String title;
    private String description;
    private Long facilitatorId;
    private String facilitatorName;
    private LocalDateTime createdAt;
}
