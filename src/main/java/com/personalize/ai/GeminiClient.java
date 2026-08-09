package com.personalize.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiClient {

    private final RestTemplate restTemplate;

    @Value("${gemini.api-key:}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    public String generateContent(String prompt, boolean jsonMode) {
        if (apiKey == null || apiKey.trim().isEmpty() || "mock-key".equalsIgnoreCase(apiKey)) {
            log.warn("Gemini API key is not configured or is set to mock. Returning mock response.");
            return getMockResponse(prompt);
        }

        try {
            String url = GEMINI_API_URL + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> partContainer = new HashMap<>();
            partContainer.put("parts", Collections.singletonList(textPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(partContainer));

            if (jsonMode) {
                Map<String, Object> generationConfig = new HashMap<>();
                generationConfig.put("responseMimeType", "application/json");
                requestBody.put("generationConfig", generationConfig);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return extractTextFromResponse(response.getBody());
            } else {
                log.error("Failed to generate content from Gemini API. Status: {}", response.getStatusCode());
                return getFallbackResponse(prompt);
            }
        } catch (Exception e) {
            log.error("Error communicating with Gemini API: {}", e.getMessage());
            return getFallbackResponse(prompt);
        }
    }

    private String extractTextFromResponse(Map responseBody) {
        try {
            List candidates = (List) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                if (content != null) {
                    List parts = (List) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map part = (Map) parts.get(0);
                        return (String) part.get("text");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Gemini API response: {}", e.getMessage());
        }
        return null;
    }

    private String getMockResponse(String prompt) {
        if (prompt.contains("LEARNING_GAP")) {
            return "{\n" +
                    "  \"summary\": \"The student shows strong understanding of basic OOP principles but struggles with advanced concurrency and Java collections indexing. Concurrency errors show issues with lock synchronization.\",\n" +
                    "  \"priorityTopics\": [\n" +
                    "    {\n" +
                    "      \"topic\": \"Multithreading\",\n" +
                    "      \"priority\": \"HIGH\",\n" +
                    "      \"reason\": \"Concurrently modifying collections leads to Race Conditions, which are critical in production web apps.\",\n" +
                    "      \"recommendedActions\": [\"Revise ExecutorService\", \"Study Thread Safety in Collections\"]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
        } else if (prompt.contains("STUDY_PLAN")) {
            return "{\n" +
                    "  \"title\": \"Java Concurrency & Collections Mastery\",\n" +
                    "  \"description\": \"A personalized path to master Collections and Concurrency\",\n" +
                    "  \"items\": [\n" +
                    "    {\n" +
                    "      \"topic\": \"Multithreading\",\n" +
                    "      \"content\": \"Watch tutorial on Java Memory Model and Synchronization primitives. Implement a thread-safe Queue.\",\n" +
                    "      \"estimatedMinutes\": 45,\n" +
                    "      \"priority\": \"HIGH\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"topic\": \"Collections\",\n" +
                    "      \"content\": \"Implement custom HashMap and analyze hash collisions. Learn difference between ConcurrentHashMap and Hashtable.\",\n" +
                    "      \"estimatedMinutes\": 60,\n" +
                    "      \"priority\": \"HIGH\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
        }
        return "{\"message\": \"Mock response from Gemini Client\"}";
    }

    private String getFallbackResponse(String prompt) {
        log.warn("Gemini service unavailable. Using safe fallback.");
        return getMockResponse(prompt);
    }
}
