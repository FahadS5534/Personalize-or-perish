package com.personalize.dto.submission;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerResponse {
    private Long id;
    private Long questionId;
    private String questionText;
    private String selectedAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
}
