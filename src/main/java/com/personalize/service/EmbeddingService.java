package com.personalize.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api-key:}")
    private String apiKey;

    private static final String EMBEDDING_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key=";

    public List<Double> getEmbedding(String text) {
        if (apiKey == null || apiKey.trim().isEmpty() || "mock-key".equalsIgnoreCase(apiKey)) {
            log.warn("Gemini API key is not configured or mock. Generating mock embedding vector.");
            return generateMockVector(text);
        }

        try {
            String url = EMBEDDING_API_URL + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", text);

            Map<String, Object> partsContainer = new HashMap<>();
            partsContainer.put("parts", Collections.singletonList(textPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "models/text-embedding-004");
            requestBody.put("content", partsContainer);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map body = response.getBody();
                Map embeddingMap = (Map) body.get("embedding");
                if (embeddingMap != null) {
                    List values = (List) embeddingMap.get("values");
                    if (values != null) {
                        List<Double> doubleValues = new ArrayList<>();
                        for (Object val : values) {
                            if (val instanceof Number) {
                                doubleValues.add(((Number) val).doubleValue());
                            }
                        }
                        return doubleValues;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate embedding from Gemini API: {}", e.getMessage());
        }

        return generateMockVector(text);
    }

    private List<Double> generateMockVector(String text) {
        Random rand = new Random(text.hashCode());
        List<Double> vector = new ArrayList<>();
        for (int i = 0; i < 768; i++) {
            vector.add(rand.nextDouble() * 2.0 - 1.0);
        }
        return vector;
    }
}
