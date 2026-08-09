package com.personalize.dto.topic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopicRequest {

    @NotBlank(message = "Topic name is required")
    private String name;

    private String description;
}
