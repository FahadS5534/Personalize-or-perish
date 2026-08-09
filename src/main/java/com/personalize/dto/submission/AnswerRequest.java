package com.personalize.dto.submission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AnswerRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotBlank(message = "Selected answer is required")
    @Pattern(regexp = "^[A-D]$", message = "Selected answer must be A, B, C, or D")
    private String selectedAnswer;
}
