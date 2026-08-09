package com.personalize.dto.question;

import com.personalize.entity.Difficulty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionResponse {
    private Long id;
    private Long assessmentId;
    private Long topicId;
    private String topicName;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private Difficulty difficulty;
}
