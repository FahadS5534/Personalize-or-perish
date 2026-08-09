package com.personalize.dto.document;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {
    private Long id;
    private String filename;
    private String title;
    private Long topicId;
    private String topicName;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
}
