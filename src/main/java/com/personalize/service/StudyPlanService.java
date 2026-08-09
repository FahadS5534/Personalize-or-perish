package com.personalize.service;

import com.personalize.dto.learning.StudyPlanInterpretation;
import com.personalize.dto.learning.StudyPlanResponse;
import com.personalize.dto.learning.StudyPlanItemResponse;
import com.personalize.entity.*;
import com.personalize.exception.BadRequestException;
import com.personalize.exception.ResourceNotFoundException;
import com.personalize.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final StudyPlanItemRepository studyPlanItemRepository;
    private final LearningGapRepository learningGapRepository;
    private final StudyMaterialRepository studyMaterialRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final GeminiService geminiService;

    @Transactional
    public StudyPlanResponse generateStudyPlan(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        if (student.getRole() != Role.STUDENT) {
            throw new BadRequestException("Only students can have study plans.");
        }

        List<LearningGap> gaps = learningGapRepository.findByStudentId(studentId);
        if (gaps.isEmpty()) {
            throw new BadRequestException("No learning gaps found. Complete an assessment to identify knowledge gaps first.");
        }

        // Build text summary of learning gaps
        StringBuilder gapsSummary = new StringBuilder();
        gapsSummary.append(String.format("Student Name: %s\n", student.getName()));
        for (LearningGap gap : gaps) {
            gapsSummary.append(String.format("- Topic: %s, Severity: %s, Topic Accuracy: %.1f%%, Explanation: %s\n",
                    gap.getTopic().getName(),
                    gap.getSeverity().name(),
                    gap.getAccuracy(),
                    gap.getExplanation() != null ? gap.getExplanation() : "N/A"));
        }

        // Gather existing study materials for these topics to provide as context
        StringBuilder materialsContext = new StringBuilder();
        for (LearningGap gap : gaps) {
            List<StudyMaterial> materials = studyMaterialRepository.findByTopicId(gap.getTopic().getId());
            for (StudyMaterial mat : materials) {
                materialsContext.append(String.format("- Topic: %s, Title: %s, Material Snippet: %s...\n",
                        gap.getTopic().getName(),
                        mat.getTitle(),
                        mat.getContent().length() > 200 ? mat.getContent().substring(0, 200) : mat.getContent()));
            }
        }

        // Call Gemini
        StudyPlanInterpretation interpretation = geminiService.generateStudyPlan(
                gapsSummary.toString(),
                materialsContext.isEmpty() ? "No specific study materials uploaded yet." : materialsContext.toString()
        );

        if (interpretation == null) {
            throw new BadRequestException("Failed to generate study plan from AI engine.");
        }

        // Save StudyPlan
        StudyPlan studyPlan = StudyPlan.builder()
                .student(student)
                .title(interpretation.getTitle())
                .description(interpretation.getDescription())
                .build();

        studyPlan = studyPlanRepository.save(studyPlan);

        List<StudyPlanItem> items = new ArrayList<>();
        for (StudyPlanInterpretation.StudyPlanItemDto itemDto : interpretation.getItems()) {
            // Find topic in our database
            Optional<Topic> topicOpt = topicRepository.findByName(itemDto.getTopic().trim());
            Topic topic;
            if (topicOpt.isPresent()) {
                topic = topicOpt.get();
            } else {
                // Fail-safe: bind to first gap's topic or first topic in DB
                topic = gaps.get(0).getTopic();
            }

            StudyPlanItem item = StudyPlanItem.builder()
                    .studyPlan(studyPlan)
                    .topic(topic)
                    .content(itemDto.getContent())
                    .estimatedMinutes(itemDto.getEstimatedMinutes() != null ? itemDto.getEstimatedMinutes() : 30)
                    .priority(itemDto.getPriority() != null ? itemDto.getPriority() : "MEDIUM")
                    .completed(false)
                    .build();

            items.add(item);
        }

        studyPlanItemRepository.saveAll(items);
        studyPlan.setItems(items);

        return mapToStudyPlanResponse(studyPlan);
    }

    public List<StudyPlanResponse> getStudentStudyPlans(Long studentId) {
        return studyPlanRepository.findByStudentId(studentId).stream()
                .map(this::mapToStudyPlanResponse)
                .collect(Collectors.toList());
    }

    public StudyPlanResponse getLatestStudyPlan(Long studentId) {
        StudyPlan plan = studyPlanRepository.findFirstByStudentIdOrderByCreatedAtDesc(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("No study plan found for student id: " + studentId));
        return mapToStudyPlanResponse(plan);
    }

    @Transactional
    public StudyPlanResponse toggleItemCompleted(Long planId, Long itemId, Boolean completed) {
        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found with id: " + planId));

        StudyPlanItem item = studyPlanItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan item not found with id: " + itemId));

        if (!item.getStudyPlan().getId().equals(plan.getId())) {
            throw new BadRequestException("Item does not belong to this study plan");
        }

        item.setCompleted(completed);
        studyPlanItemRepository.save(item);

        return mapToStudyPlanResponse(plan);
    }

    private StudyPlanResponse mapToStudyPlanResponse(StudyPlan plan) {
        List<StudyPlanItemResponse> itemResponses = plan.getItems().stream()
                .map(item -> StudyPlanItemResponse.builder()
                        .id(item.getId())
                        .topicId(item.getTopic().getId())
                        .topicName(item.getTopic().getName())
                        .content(item.getContent())
                        .estimatedMinutes(item.getEstimatedMinutes())
                        .priority(item.getPriority())
                        .completed(item.getCompleted())
                        .build())
                .collect(Collectors.toList());

        return StudyPlanResponse.builder()
                .id(plan.getId())
                .studentId(plan.getStudent().getId())
                .studentName(plan.getStudent().getName())
                .title(plan.getTitle())
                .description(plan.getDescription())
                .createdAt(plan.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}
