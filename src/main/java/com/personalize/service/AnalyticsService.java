package com.personalize.service;

import com.personalize.dto.analytics.*;
import com.personalize.entity.*;
import com.personalize.exception.ResourceNotFoundException;
import com.personalize.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SubmissionRepository submissionRepository;
    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final LearningGapRepository learningGapRepository;

    public StudentAnalyticsResponse getStudentAnalytics(Long studentId) {
        List<Submission> submissions = submissionRepository.findByStudentId(studentId);
        if (submissions.isEmpty()) {
            return StudentAnalyticsResponse.builder()
                    .overallScore(0.0)
                    .topicPerformance(Collections.emptyList())
                    .strongTopics(Collections.emptyList())
                    .weakTopics(Collections.emptyList())
                    .recentAssessments(Collections.emptyList())
                    .build();
        }

        // Overall Score
        double overallScore = submissions.stream()
                .mapToDouble(Submission::getScore)
                .average()
                .orElse(0.0);

        // Topic Performance Calculation
        // Gather all answers submitted by this student
        List<Answer> allAnswers = submissions.stream()
                .flatMap(sub -> sub.getAnswers().stream())
                .collect(Collectors.toList());

        Map<Topic, List<Answer>> answersByTopic = allAnswers.stream()
                .collect(Collectors.groupingBy(ans -> ans.getQuestion().getTopic()));

        List<TopicPerformanceDto> topicPerformance = new ArrayList<>();
        List<String> strongTopics = new ArrayList<>();
        List<String> weakTopics = new ArrayList<>();

        for (Map.Entry<Topic, List<Answer>> entry : answersByTopic.entrySet()) {
            Topic topic = entry.getKey();
            List<Answer> topicAnswers = entry.getValue();

            long correctCount = topicAnswers.stream().filter(Answer::getIsCorrect).count();
            double accuracy = ((double) correctCount / topicAnswers.size()) * 100.0;

            topicPerformance.add(new TopicPerformanceDto(topic.getName(), accuracy));

            if (accuracy >= 70.0) {
                strongTopics.add(topic.getName());
            } else if (accuracy < 50.0) {
                weakTopics.add(topic.getName());
            }
        }

        // Recent Assessments
        List<RecentAssessmentDto> recentAssessments = submissions.stream()
                .map(sub -> RecentAssessmentDto.builder()
                        .assessmentId(sub.getAssessment().getId())
                        .title(sub.getAssessment().getTitle())
                        .score(sub.getScore())
                        .submittedAt(sub.getSubmittedAt())
                        .build())
                .sorted(Comparator.comparing(RecentAssessmentDto::getSubmittedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return StudentAnalyticsResponse.builder()
                .overallScore(overallScore)
                .topicPerformance(topicPerformance)
                .strongTopics(strongTopics)
                .weakTopics(weakTopics)
                .recentAssessments(recentAssessments)
                .build();
    }

    public FacilitatorAnalyticsResponse getAssessmentAnalytics(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));

        List<Submission> submissions = submissionRepository.findByAssessmentId(assessmentId);
        if (submissions.isEmpty()) {
            return FacilitatorAnalyticsResponse.builder()
                    .averageScore(0.0)
                    .highestScore(0.0)
                    .lowestScore(0.0)
                    .topicPerformance(Collections.emptyList())
                    .numberOfStudents(0)
                    .difficultQuestions(Collections.emptyList())
                    .commonLearningGaps(Collections.emptyList())
                    .build();
        }

        int numberOfStudents = submissions.size();

        double averageScore = submissions.stream()
                .mapToDouble(Submission::getScore)
                .average()
                .orElse(0.0);

        double highestScore = submissions.stream()
                .mapToDouble(Submission::getScore)
                .max()
                .orElse(0.0);

        double lowestScore = submissions.stream()
                .mapToDouble(Submission::getScore)
                .min()
                .orElse(0.0);

        // Topic performance on this specific assessment
        List<Answer> allAnswers = submissions.stream()
                .flatMap(sub -> sub.getAnswers().stream())
                .collect(Collectors.toList());

        Map<Topic, List<Answer>> answersByTopic = allAnswers.stream()
                .collect(Collectors.groupingBy(ans -> ans.getQuestion().getTopic()));

        List<TopicPerformanceDto> topicPerformance = answersByTopic.entrySet().stream()
                .map(entry -> {
                    Topic topic = entry.getKey();
                    List<Answer> topicAnswers = entry.getValue();
                    long correct = topicAnswers.stream().filter(Answer::getIsCorrect).count();
                    double accuracy = ((double) correct / topicAnswers.size()) * 100.0;
                    return new TopicPerformanceDto(topic.getName(), accuracy);
                })
                .collect(Collectors.toList());

        // Difficult Questions
        Map<Question, List<Answer>> answersByQuestion = allAnswers.stream()
                .collect(Collectors.groupingBy(Answer::getQuestion));

        List<DifficultQuestionDto> difficultQuestions = answersByQuestion.entrySet().stream()
                .map(entry -> {
                    Question question = entry.getKey();
                    List<Answer> questionAnswers = entry.getValue();
                    long incorrect = questionAnswers.stream().filter(ans -> !ans.getIsCorrect()).count();
                    double failureRate = ((double) incorrect / questionAnswers.size()) * 100.0;
                    return DifficultQuestionDto.builder()
                            .questionId(question.getId())
                            .questionText(question.getQuestionText())
                            .failureRate(failureRate)
                            .build();
                })
                .sorted(Comparator.comparing(DifficultQuestionDto::getFailureRate).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Common Learning Gaps for students who attempted this assessment
        Set<Long> studentIds = submissions.stream()
                .map(sub -> sub.getStudent().getId())
                .collect(Collectors.toSet());

        // Count severity of gaps for these students
        Map<String, Long> gapCounts = studentIds.stream()
                .flatMap(sid -> learningGapRepository.findByStudentId(sid).stream())
                .filter(gap -> gap.getSeverity() == Severity.HIGH || gap.getSeverity() == Severity.MEDIUM)
                .collect(Collectors.groupingBy(gap -> gap.getTopic().getName(), Collectors.counting()));

        List<CommonLearningGapDto> commonLearningGaps = gapCounts.entrySet().stream()
                .map(entry -> new CommonLearningGapDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CommonLearningGapDto::getCount).reversed())
                .collect(Collectors.toList());

        return FacilitatorAnalyticsResponse.builder()
                .averageScore(averageScore)
                .highestScore(highestScore)
                .lowestScore(lowestScore)
                .topicPerformance(topicPerformance)
                .numberOfStudents(numberOfStudents)
                .difficultQuestions(difficultQuestions)
                .commonLearningGaps(commonLearningGaps)
                .build();
    }
}
