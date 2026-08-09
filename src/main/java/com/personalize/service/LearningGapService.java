package com.personalize.service;

import com.personalize.dto.learning.LearningGapInterpretation;
import com.personalize.dto.learning.LearningGapResponse;
import com.personalize.entity.*;
import com.personalize.exception.ResourceNotFoundException;
import com.personalize.repository.LearningGapRepository;
import com.personalize.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningGapService {

    private final LearningGapRepository learningGapRepository;
    private final TopicRepository topicRepository;
    private final GeminiService geminiService;

    @EventListener
    @Transactional
    public void handleSubmissionEvent(Submission submission) {
        log.info("Received submission event for student id: {}. Running learning gap analysis...", submission.getStudent().getId());
        analyzeSubmissionGaps(submission);
    }

    @Transactional
    public void analyzeSubmissionGaps(Submission submission) {
        User student = submission.getStudent();
        
        Map<Topic, List<Answer>> answersByTopic = submission.getAnswers().stream()
                .collect(Collectors.groupingBy(ans -> ans.getQuestion().getTopic()));

        List<LearningGap> updatedGaps = new ArrayList<>();

        for (Map.Entry<Topic, List<Answer>> entry : answersByTopic.entrySet()) {
            Topic topic = entry.getKey();
            List<Answer> answers = entry.getValue();

            long correctCount = answers.stream().filter(Answer::getIsCorrect).count();
            double accuracy = ((double) correctCount / answers.size()) * 100.0;

            Severity severity;
            if (accuracy < 50.0) {
                severity = Severity.HIGH;
            } else if (accuracy < 70.0) {
                severity = Severity.MEDIUM;
            } else {
                severity = Severity.LOW;
            }

            Optional<LearningGap> existingGapOpt = learningGapRepository.findByStudentIdAndTopicId(student.getId(), topic.getId());
            LearningGap gap;
            if (existingGapOpt.isPresent()) {
                gap = existingGapOpt.get();
                gap.setAccuracy(accuracy);
                gap.setSeverity(severity);
            } else {
                gap = LearningGap.builder()
                        .student(student)
                        .topic(topic)
                        .accuracy(accuracy)
                        .severity(severity)
                        .build();
            }

            gap = learningGapRepository.save(gap);
            updatedGaps.add(gap);
        }

        try {
            StringBuilder performanceSummary = new StringBuilder();
            performanceSummary.append(String.format("Student: %s\n", student.getName()));
            for (LearningGap g : updatedGaps) {
                performanceSummary.append(String.format("- Topic: %s, Accuracy: %.1f%%, Deterministic Severity: %s\n",
                        g.getTopic().getName(), g.getAccuracy(), g.getSeverity().name()));
            }

            LearningGapInterpretation interpretation = geminiService.interpretGaps(performanceSummary.toString());
            if (interpretation != null) {
                Map<String, LearningGapInterpretation.PriorityTopic> priorityMap = interpretation.getPriorityTopics().stream()
                        .collect(Collectors.toMap(
                                p -> p.getTopic().trim().toLowerCase(),
                                p -> p,
                                (p1, p2) -> p1
                        ));

                for (LearningGap gap : updatedGaps) {
                    String topicNameKey = gap.getTopic().getName().trim().toLowerCase();
                    LearningGapInterpretation.PriorityTopic priorityTopic = priorityMap.get(topicNameKey);
                    if (priorityTopic != null) {
                        String explanation = String.format("Summary: %s\nReason: %s\nRecommended Actions: %s",
                                interpretation.getSummary(),
                                priorityTopic.getReason(),
                                String.join(", ", priorityTopic.getRecommendedActions()));
                        gap.setExplanation(explanation);
                    } else {
                        gap.setExplanation(String.format("Summary: %s\nDeterministic performance assessment.", interpretation.getSummary()));
                    }
                    learningGapRepository.save(gap);
                }
            }
        } catch (Exception e) {
            log.error("Failed to run Gemini interpretation for learning gaps: {}", e.getMessage());
            for (LearningGap gap : updatedGaps) {
                if (gap.getExplanation() == null) {
                    gap.setExplanation("Deterministic evaluation complete. Gemini interpretation currently unavailable.");
                    learningGapRepository.save(gap);
                }
            }
        }
    }

    public List<LearningGapResponse> getStudentLearningGaps(Long studentId) {
        return learningGapRepository.findByStudentId(studentId).stream()
                .map(this::mapToLearningGapResponse)
                .collect(Collectors.toList());
    }

    private LearningGapResponse mapToLearningGapResponse(LearningGap gap) {
        return LearningGapResponse.builder()
                .id(gap.getId())
                .studentId(gap.getStudent().getId())
                .studentName(gap.getStudent().getName())
                .topicId(gap.getTopic().getId())
                .topicName(gap.getTopic().getName())
                .severity(gap.getSeverity())
                .accuracy(gap.getAccuracy())
                .explanation(gap.getExplanation())
                .identifiedAt(gap.getIdentifiedAt())
                .build();
    }
}
