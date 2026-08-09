package com.personalize.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    public <T> T parse(String content, Class<T> clazz) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        String cleaned = cleanJson(content);
        try {
            return objectMapper.readValue(cleaned, clazz);
        } catch (Exception e) {
            log.error("Failed to parse AI response to class {}: {}", clazz.getSimpleName(), e.getMessage());
            log.debug("Original Content: {}", content);
            log.debug("Cleaned Content: {}", cleaned);
            return null;
        }
    }

    private String cleanJson(String raw) {
        String clean = raw.trim();
        if (clean.startsWith("```")) {
            int firstNewLine = clean.indexOf('\n');
            if (firstNewLine != -1) {
                clean = clean.substring(firstNewLine + 1);
            } else {
                clean = clean.substring(3);
            }
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length() - 3);
        }
        return clean.trim();
    }
}
