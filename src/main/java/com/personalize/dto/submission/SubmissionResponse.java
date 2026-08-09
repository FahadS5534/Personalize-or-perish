package com.personalize.dto.submission;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SubmissionResponse {
    private Long id;
    private Long assessmentId;
    private String assessmentTitle;
    private Long studentId;
    private String studentName;
    private Double score;
    private LocalDateTime submittedAt;
    private List<AnswerResponse> answers;
}
