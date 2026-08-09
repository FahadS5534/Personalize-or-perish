package com.personalize.repository;

import com.personalize.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByAssessmentId(Long assessmentId);
    List<Question> findByAssessmentIdAndTopicId(Long assessmentId, Long topicId);
}
