package com.personalize.dto.material;

import com.personalize.entity.Difficulty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudyMaterialRequest {

    @NotNull(message = "Topic ID is required")
    private Long topicId;

    @NotNull(message = "Difficulty level is required")
    private Difficulty difficulty;
}
