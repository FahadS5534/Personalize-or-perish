package com.personalize.dto.material;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudyMaterialResponse {
    private Long id;
    private Long topicId;
    private String topicName;
    private String title;
    private String content;
    private String type; // "GENERATED", "MANUAL"
}
