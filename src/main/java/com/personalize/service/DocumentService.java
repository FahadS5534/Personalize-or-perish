package com.personalize.service;

import com.personalize.dto.document.DocumentResponse;
import com.personalize.entity.Document;
import com.personalize.entity.Topic;
import com.personalize.entity.User;
import com.personalize.exception.BadRequestException;
import com.personalize.exception.ResourceNotFoundException;
import com.personalize.repository.DocumentRepository;
import com.personalize.repository.TopicRepository;
import com.personalize.vector.ChromaDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final TopicRepository topicRepository;
    private final ChromaDocumentService chromaDocumentService;
    private final UserService userService;

    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));

        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        User currentUser = userService.getCurrentUser();

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String filename = file.getOriginalFilename();
            if (filename == null) {
                filename = "uploaded_file";
            }

            Document document = Document.builder()
                    .title(filename)
                    .filename(filename)
                    .topic(topic)
                    .uploadedBy(currentUser)
                    .build();

            document = documentRepository.save(document);

            // Index in Chroma Vector DB
            chromaDocumentService.indexDocument(document.getId(), topic.getName(), content);

            return mapToDocumentResponse(document);
        } catch (Exception e) {
            log.error("Failed to process and index document: {}", e.getMessage());
            throw new BadRequestException("Failed to upload document: " + e.getMessage());
        }
    }

    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        documentRepository.delete(document);
    }

    private DocumentResponse mapToDocumentResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .filename(document.getFilename())
                .title(document.getTitle())
                .topicId(document.getTopic().getId())
                .topicName(document.getTopic().getName())
                .uploadedById(document.getUploadedBy().getId())
                .uploadedByName(document.getUploadedBy().getName())
                .uploadedAt(document.getCreatedAt())
                .build();
    }
}
