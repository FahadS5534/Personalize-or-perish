package com.personalize.vector;

import com.personalize.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChromaDocumentService {

    private final ChromaClient chromaClient;
    private final EmbeddingService embeddingService;

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 100;

    public void indexDocument(Long documentId, String topicName, String content) {
        log.info("Starting chunking and indexing for document ID: {}, topic: {}", documentId, topicName);
        
        List<String> chunks = chunkText(content, CHUNK_SIZE, CHUNK_OVERLAP);
        
        List<String> ids = new ArrayList<>();
        List<List<Double>> embeddings = new ArrayList<>();
        List<String> documents = new ArrayList<>();
        List<Map<String, Object>> metadatas = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            String chunkId = "doc_" + documentId + "_chunk_" + i;
            
            List<Double> vector = embeddingService.getEmbedding(chunkText);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentId", documentId);
            metadata.put("topic", topicName);
            metadata.put("chunkIndex", i);

            ids.add(chunkId);
            embeddings.add(vector);
            documents.add(chunkText);
            metadatas.add(metadata);
        }

        chromaClient.addDocumentChunks(ids, embeddings, documents, metadatas);
        log.info("Completed indexing of {} chunks for document ID: {}", chunks.size(), documentId);
    }

    public void indexStudyMaterial(Long materialId, String topicName, String content) {
        log.info("Starting chunking and indexing for study material ID: {}, topic: {}", materialId, topicName);
        
        List<String> chunks = chunkText(content, CHUNK_SIZE, CHUNK_OVERLAP);
        
        List<String> ids = new ArrayList<>();
        List<List<Double>> embeddings = new ArrayList<>();
        List<String> documents = new ArrayList<>();
        List<Map<String, Object>> metadatas = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            String chunkId = "material_" + materialId + "_chunk_" + i;
            
            List<Double> vector = embeddingService.getEmbedding(chunkText);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("materialId", materialId);
            metadata.put("topic", topicName);
            metadata.put("chunkIndex", i);

            ids.add(chunkId);
            embeddings.add(vector);
            documents.add(chunkText);
            metadatas.add(metadata);
        }

        chromaClient.addDocumentChunks(ids, embeddings, documents, metadatas);
        log.info("Completed indexing of {} chunks for study material ID: {}", chunks.size(), materialId);
    }


    public List<ChromaClient.QueryResult> queryRelevantContext(String queryText, String topicName, int maxResults) {
        List<Double> queryEmbedding = embeddingService.getEmbedding(queryText);
        List<ChromaClient.QueryResult> allResults = chromaClient.queryCollection(queryEmbedding, maxResults * 2);

        if (topicName != null && !topicName.trim().isEmpty()) {
            List<ChromaClient.QueryResult> filtered = new ArrayList<>();
            for (ChromaClient.QueryResult res : allResults) {
                if (res.getMetadata() != null) {
                    String docTopic = (String) res.getMetadata().get("topic");
                    if (topicName.equalsIgnoreCase(docTopic)) {
                        filtered.add(res);
                    }
                }
                if (filtered.size() >= maxResults) {
                    break;
                }
            }
            return filtered;
        }

        return allResults.size() > maxResults ? allResults.subList(0, maxResults) : allResults;
    }

    private List<String> chunkText(String text, int size, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + size, length);
            chunks.add(text.substring(start, end));
            
            if (end == length) {
                break;
            }
            start += (size - overlap);
        }

        return chunks;
    }
}
