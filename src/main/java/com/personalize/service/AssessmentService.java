package com.personalize.service;

import com.personalize.dto.assessment.AssessmentRequest;
import com.personalize.dto.assessment.AssessmentResponse;
import com.personalize.dto.question.QuestionRequest;
import com.personalize.dto.question.QuestionResponse;
import com.personalize.entity.Assessment;
import com.personalize.entity.Question;
import com.personalize.entity.Topic;
import com.personalize.entity.User;
import com.personalize.exception.BadRequestException;
import com.personalize.exception.ResourceNotFoundException;
import com.personalize.repository.AssessmentRepository;
import com.personalize.repository.QuestionRepository;
import com.personalize.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;

    @Transactional
    public AssessmentResponse createAssessment(AssessmentRequest request, User facilitator) {
        Assessment assessment = Assessment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .facilitator(facilitator)
                .build();
        
        assessment = assessmentRepository.save(assessment);
        return mapToAssessmentResponse(assessment);
    }

    public List<AssessmentResponse> getAllAssessments() {
        return assessmentRepository.findAll().stream()
                .map(this::mapToAssessmentResponse)
                .collect(Collectors.toList());
    }

    public AssessmentResponse getAssessmentResponseById(Long id) {
        Assessment assessment = getAssessmentById(id);
        return mapToAssessmentResponse(assessment);
    }

    public Assessment getAssessmentById(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));
    }

    @Transactional
    public AssessmentResponse updateAssessment(Long id, AssessmentRequest request, User facilitator) {
        Assessment assessment = getAssessmentById(id);
        if (!assessment.getFacilitator().getId().equals(facilitator.getId())) {
            throw new BadRequestException("You do not own this assessment");
        }
        assessment.setTitle(request.getTitle());
        assessment.setDescription(request.getDescription());
        assessment = assessmentRepository.save(assessment);
        return mapToAssessmentResponse(assessment);
    }

    @Transactional
    public void deleteAssessment(Long id, User facilitator) {
        Assessment assessment = getAssessmentById(id);
        if (!assessment.getFacilitator().getId().equals(facilitator.getId())) {
            throw new BadRequestException("You do not own this assessment");
        }
        assessmentRepository.delete(assessment);
    }

    @Transactional
    public QuestionResponse addQuestion(Long assessmentId, QuestionRequest request, User facilitator) {
        Assessment assessment = getAssessmentById(assessmentId);
        if (!assessment.getFacilitator().getId().equals(facilitator.getId())) {
            throw new BadRequestException("You do not own this assessment");
        }

        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + request.getTopicId()));

        Question question = Question.builder()
                .assessment(assessment)
                .topic(topic)
                .questionText(request.getQuestionText())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .correctAnswer(request.getCorrectAnswer())
                .difficulty(request.getDifficulty())
                .build();

        question = questionRepository.save(question);
        return mapToQuestionResponse(question);
    }

    public List<QuestionResponse> getQuestions(Long assessmentId) {
        getAssessmentById(assessmentId);
        
        return questionRepository.findByAssessmentId(assessmentId).stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }

    private AssessmentResponse mapToAssessmentResponse(Assessment assessment) {
        return AssessmentResponse.builder()
                .id(assessment.getId())
                .title(assessment.getTitle())
                .description(assessment.getDescription())
                .facilitatorId(assessment.getFacilitator().getId())
                .facilitatorName(assessment.getFacilitator().getName())
                .createdAt(assessment.getCreatedAt())
                .build();
    }

    private QuestionResponse mapToQuestionResponse(Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .assessmentId(question.getAssessment().getId())
                .topicId(question.getTopic().getId())
                .topicName(question.getTopic().getName())
                .questionText(question.getQuestionText())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .correctAnswer(question.getCorrectAnswer())
                .difficulty(question.getDifficulty())
                .build();
    }
}
