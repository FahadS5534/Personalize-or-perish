package com.personalize.repository;

import com.personalize.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByStudentId(Long studentId);
    List<Submission> findByAssessmentId(Long assessmentId);
    List<Submission> findByStudentIdAndAssessmentId(Long studentId, Long assessmentId);
}
