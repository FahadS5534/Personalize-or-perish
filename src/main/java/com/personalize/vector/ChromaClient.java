package com.personalize.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChromaClient {

    private final RestTemplate restTemplate;

    @Value("${chroma.url:http://localhost:8000}")
    private String chromaUrl;

    private static final String COLLECTION_NAME = "academic_collection";
    private String collectionId = null;
    private boolean isHealthy = false;

    @PostConstruct
    public void init() {
        try {
            String healthUrl = chromaUrl + "/api/v1/heartbeat";
            try {
                restTemplate.getForObject(healthUrl, Map.class);
                isHealthy = true;
                log.info("ChromaDB is running and connected successfully at {}", chromaUrl);
            } catch (Exception e) {
                log.warn("ChromaDB heartbeat failed at {}. Operating in mock/fallback vector store mode.", chromaUrl);
                isHealthy = false;
                return;
            }

            collectionId = getOrCreateCollection(COLLECTION_NAME);
            log.info("ChromaDB Collection '{}' resolved with ID: {}", COLLECTION_NAME, collectionId);
        } catch (Exception e) {
            log.error("Failed to initialize ChromaDB Client: {}", e.getMessage());
            isHealthy = false;
        }
    }

    private String getOrCreateCollection(String name) {
        String url = chromaUrl + "/api/v1/collections";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("hnsw:space", "cosine");
        body.put("metadata", metadata);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("id");
            }
        } catch (Exception e) {
            try {
                String getUrl = chromaUrl + "/api/v1/collections/" + name;
                Map existing = restTemplate.getForObject(getUrl, Map.class);
                if (existing != null) {
                    return (String) existing.get("id");
                }
            } catch (Exception ex) {
                log.error("Failed to get/create Chroma collection: {}", ex.getMessage());
            }
        }
        return null;
    }

    public void addDocumentChunks(List<String> ids, List<List<Double>> embeddings, List<String> documents, List<Map<String, Object>> metadatas) {
        if (!isHealthy || collectionId == null) {
            log.warn("ChromaDB is down or uninitialized. Skipping vector storage chunk insertion.");
            return;
        }

        try {
            String url = chromaUrl + "/api/v1/collections/" + collectionId + "/add";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("ids", ids);
            body.put("embeddings", embeddings);
            body.put("documents", documents);
            body.put("metadatas", metadatas);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, entity, Map.class);
            log.info("Successfully added {} chunks to ChromaDB", ids.size());
        } catch (Exception e) {
            log.error("Failed to add document chunks to ChromaDB: {}", e.getMessage());
        }
    }

    public List<QueryResult> queryCollection(List<Double> queryEmbedding, int nResults) {
        if (!isHealthy || collectionId == null) {
            log.warn("ChromaDB is down or uninitialized. Returning empty vector query results.");
            return Collections.emptyList();
        }

        try {
            String url = chromaUrl + "/api/v1/collections/" + collectionId + "/query";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("query_embeddings", Collections.singletonList(queryEmbedding));
            body.put("n_results", nResults);
            body.put("include", Arrays.asList("documents", "metadatas", "distances"));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseQueryResponse(response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to query collection in ChromaDB: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<QueryResult> parseQueryResponse(Map responseBody) {
        List<QueryResult> results = new ArrayList<>();
        try {
            List idsList = (List) responseBody.get("ids");
            List documentsList = (List) responseBody.get("documents");
            List metadatasList = (List) responseBody.get("metadatas");
            List distancesList = (List) responseBody.get("distances");

            if (idsList != null && !idsList.isEmpty()) {
                List ids = (List) idsList.get(0);
                List documents = (List) documentsList.get(0);
                List metadatas = (List) metadatasList.get(0);
                List distances = (List) distancesList.get(0);

                for (int i = 0; i < ids.size(); i++) {
                    QueryResult res = new QueryResult();
                    res.setId((String) ids.get(i));
                    res.setDocument((String) documents.get(i));
                    res.setMetadata((Map<String, Object>) metadatas.get(i));
                    res.setDistance(((Number) distances.get(i)).doubleValue());
                    results.add(res);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Chroma query response: {}", e.getMessage());
        }
        return results;
    }

    @lombok.Data
    public static class QueryResult {
        private String id;
        private String document;
        private Map<String, Object> metadata;
        private Double distance;
    }
}
