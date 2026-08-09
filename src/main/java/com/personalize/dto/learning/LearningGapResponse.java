package com.personalize.dto.learning;

import com.personalize.entity.Severity;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class LearningGapResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long topicId;
    private String topicName;
    private Severity severity;
    private Double accuracy;
    private String explanation;
    private LocalDateTime identifiedAt;
}
