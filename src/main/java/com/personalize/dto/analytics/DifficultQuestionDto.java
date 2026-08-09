package com.personalize.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DifficultQuestionDto {
    private Long questionId;
    private String questionText;
    private Double failureRate;
}
