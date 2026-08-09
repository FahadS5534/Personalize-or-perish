package com.personalize.dto.topic;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopicResponse {
    private Long id;
    private String name;
    private String description;
}
