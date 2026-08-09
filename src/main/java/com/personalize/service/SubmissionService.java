package com.personalize.service;

import com.personalize.dto.submission.AnswerRequest;
import com.personalize.dto.submission.AnswerResponse;
import com.personalize.dto.submission.SubmissionRequest;
import com.personalize.dto.submission.SubmissionResponse;
import com.personalize.entity.*;
import com.personalize.exception.BadRequestException;
import com.personalize.exception.ResourceNotFoundException;
import com.personalize.repository.AssessmentRepository;
import com.personalize.repository.QuestionRepository;
import com.personalize.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SubmissionResponse submitAssessment(SubmissionRequest request, User student) {
        Assessment assessment = assessmentRepository.findById(request.getAssessmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + request.getAssessmentId()));

        List<Question> questions = questionRepository.findByAssessmentId(assessment.getId());
        if (questions.isEmpty()) {
            throw new BadRequestException("Assessment has no questions. Cannot submit.");
        }

        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        double correctCount = 0;
        List<Answer> answers = new ArrayList<>();

        Submission submission = Submission.builder()
                .assessment(assessment)
                .student(student)
                .score(0.0)
                .build();

        for (AnswerRequest ansReq : request.getAnswers()) {
            Question question = questionMap.get(ansReq.getQuestionId());
            if (question == null) {
                throw new BadRequestException("Question ID " + ansReq.getQuestionId() + " does not belong to this assessment");
            }

            boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(ansReq.getSelectedAnswer().trim());
            if (isCorrect) {
                correctCount++;
            }

            Answer answer = Answer.builder()
                    .submission(submission)
                    .question(question)
                    .selectedAnswer(ansReq.getSelectedAnswer().trim())
                    .isCorrect(isCorrect)
                    .build();

            answers.add(answer);
        }

        double scorePercentage = (correctCount / questions.size()) * 100.0;
        submission.setScore(scorePercentage);
        submission.setAnswers(answers);

        submission = submissionRepository.save(submission);

        // Publish event to trigger learning gap analysis
        eventPublisher.publishEvent(submission);

        return mapToSubmissionResponse(submission);
    }

    public SubmissionResponse getSubmissionDetails(Long id, User user) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        if (user.getRole() == Role.STUDENT && !submission.getStudent().getId().equals(user.getId())) {
            throw new BadRequestException("You can only view your own submissions");
        }

        return mapToSubmissionResponse(submission);
    }

    public List<SubmissionResponse> getSubmissions(Long studentId, Long assessmentId, User user) {
        List<Submission> submissions;

        if (user.getRole() == Role.STUDENT) {
            if (assessmentId != null) {
                submissions = submissionRepository.findByStudentIdAndAssessmentId(user.getId(), assessmentId);
            } else {
                submissions = submissionRepository.findByStudentId(user.getId());
            }
        } else {
            if (studentId != null && assessmentId != null) {
                submissions = submissionRepository.findByStudentIdAndAssessmentId(studentId, assessmentId);
            } else if (studentId != null) {
                submissions = submissionRepository.findByStudentId(studentId);
            } else if (assessmentId != null) {
                submissions = submissionRepository.findByAssessmentId(assessmentId);
            } else {
                submissions = submissionRepository.findAll();
            }
        }

        return submissions.stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList());
    }

    private SubmissionResponse mapToSubmissionResponse(Submission submission) {
        List<AnswerResponse> answerResponses = submission.getAnswers().stream()
                .map(ans -> AnswerResponse.builder()
                        .id(ans.getId())
                        .questionId(ans.getQuestion().getId())
                        .questionText(ans.getQuestion().getQuestionText())
                        .selectedAnswer(ans.getSelectedAnswer())
                        .correctAnswer(ans.getQuestion().getCorrectAnswer())
                        .isCorrect(ans.getIsCorrect())
                        .build())
                .collect(Collectors.toList());

        return SubmissionResponse.builder()
                .id(submission.getId())
                .assessmentId(submission.getAssessment().getId())
                .assessmentTitle(submission.getAssessment().getTitle())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getName())
                .score(submission.getScore())
                .submittedAt(submission.getSubmittedAt())
                .answers(answerResponses)
                .build();
    }
}
