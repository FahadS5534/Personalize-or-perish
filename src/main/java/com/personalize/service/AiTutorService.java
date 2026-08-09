package com.personalize.service;

import com.personalize.dto.tutor.ChatRequest;
import com.personalize.dto.tutor.ChatResponse;
import com.personalize.entity.Document;
import com.personalize.entity.Topic;
import com.personalize.repository.DocumentRepository;
import com.personalize.repository.TopicRepository;
import com.personalize.vector.ChromaClient;
import com.personalize.vector.ChromaDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiTutorService {

    private final ChromaDocumentService chromaDocumentService;
    private final DocumentRepository documentRepository;
    private final TopicRepository topicRepository;
    private final GeminiService geminiService;

    public ChatResponse chat(ChatRequest request) {
        String topicName = null;
        if (request.getTopicId() != null) {
            Topic topic = topicRepository.findById(request.getTopicId()).orElse(null);
            if (topic != null) {
                topicName = topic.getName();
            }
        }

        // Query Chroma for relevant chunks
        List<ChromaClient.QueryResult> queryResults = chromaDocumentService.queryRelevantContext(
                request.getMessage(),
                topicName,
                3
        );

        StringBuilder contextBuilder = new StringBuilder();
        Set<String> sources = new HashSet<>();

        for (ChromaClient.QueryResult result : queryResults) {
            contextBuilder.append(result.getDocument()).append("\n---\n");
            
            // Extract document ID from metadata to resolve filenames
            if (result.getMetadata() != null && result.getMetadata().containsKey("documentId")) {
                try {
                    Number docIdNum = (Number) result.getMetadata().get("documentId");
                    Long docId = docIdNum.longValue();
                    Document doc = documentRepository.findById(docId).orElse(null);
                    if (doc != null) {
                        sources.add(doc.getFilename());
                    }
                } catch (Exception e) {
                    log.error("Failed to parse document ID from chunk metadata: {}", e.getMessage());
                }
            }
        }

        String context = contextBuilder.isEmpty() ? "No specific course materials found for this question." : contextBuilder.toString();

        String prompt = String.format("""
                You are a helpful, professional AI Tutor in the 'Personalize or Perish' academic system.
                Your goal is to answer the user's programming questions using the provided context materials.
                
                Context Materials:
                %s
                
                User Question: %s
                
                Answer the question accurately, explaining concepts simply. Reference specific course materials if available.
                """, context, request.getMessage());

        String reply = geminiService.generateGenericContent(prompt);

        return ChatResponse.builder()
                .reply(reply)
                .sources(sources)
                .build();
    }
}
