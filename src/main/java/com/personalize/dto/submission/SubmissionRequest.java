package com.personalize.dto.submission;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class SubmissionRequest {

    @NotNull(message = "Assessment ID is required")
    private Long assessmentId;

    @NotEmpty(message = "Answers list cannot be empty")
    private List<AnswerRequest> answers;
}
