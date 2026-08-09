package com.personalize.dto.tutor;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    private String message;

    private Long topicId;
}
