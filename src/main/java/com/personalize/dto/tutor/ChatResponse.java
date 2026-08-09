package com.personalize.dto.tutor;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class ChatResponse {
    private String reply;
    private Set<String> sources;
}
