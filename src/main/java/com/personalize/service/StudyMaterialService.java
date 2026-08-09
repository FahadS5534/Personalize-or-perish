package com.personalize.service;

import com.personalize.dto.material.StudyMaterialRequest;
import com.personalize.dto.material.StudyMaterialResponse;
import com.personalize.entity.StudyMaterial;
import com.personalize.entity.Topic;
import com.personalize.exception.BadRequestException;
import com.personalize.exception.ResourceNotFoundException;
import com.personalize.repository.StudyMaterialRepository;
import com.personalize.repository.TopicRepository;
import com.personalize.vector.ChromaDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyMaterialService {

    private final StudyMaterialRepository studyMaterialRepository;
    private final TopicRepository topicRepository;
    private final GeminiService geminiService;
    private final ChromaDocumentService chromaDocumentService;

    @Transactional
    public StudyMaterialResponse generateStudyMaterial(StudyMaterialRequest request) {
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + request.getTopicId()));

        try {
            String prompt = String.format("""
                    Generate a comprehensive, high-quality programming study guide for the topic: '%s'.
                    Target Audience Difficulty Level: %s
                    
                    Provide a full guide with:
                    1. Core concepts explained simply.
                    2. Working code snippets with explanations.
                    3. Common mistakes/pitfalls.
                    4. 3 practice exercises with hints.
                    """, topic.getName(), request.getDifficulty().name());

            String content = geminiService.generateGenericContent(prompt);
            String title = String.format("Mastering %s (%s)", topic.getName(), request.getDifficulty().name());

            StudyMaterial material = StudyMaterial.builder()
                    .title(title)
                    .content(content)
                    .topic(topic)
                    .type("GENERATED")
                    .build();

            material = studyMaterialRepository.save(material);

            chromaDocumentService.indexStudyMaterial(material.getId(), topic.getName(), content);

            return mapToResponse(material);
        } catch (Exception e) {
            log.error("Failed to generate and index study material: {}", e.getMessage());
            throw new BadRequestException("Failed to generate study material: " + e.getMessage());
        }
    }

    public List<StudyMaterialResponse> getMaterialsByTopic(Long topicId) {
        return studyMaterialRepository.findByTopicId(topicId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<StudyMaterialResponse> getAllMaterials() {
        return studyMaterialRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private StudyMaterialResponse mapToResponse(StudyMaterial material) {
        return StudyMaterialResponse.builder()
                .id(material.getId())
                .topicId(material.getTopic().getId())
                .topicName(material.getTopic().getName())
                .title(material.getTitle())
                .content(material.getContent())
                .type(material.getType())
                .build();
    }
}
