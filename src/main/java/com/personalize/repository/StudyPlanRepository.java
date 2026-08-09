package com.personalize.repository;

import com.personalize.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    List<StudyPlan> findByStudentId(Long studentId);
    Optional<StudyPlan> findFirstByStudentIdOrderByCreatedAtDesc(Long studentId);
}
