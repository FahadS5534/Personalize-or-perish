package com.personalize.repository;

import com.personalize.entity.LearningGap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningGapRepository extends JpaRepository<LearningGap, Long> {
    List<LearningGap> findByStudentId(Long studentId);
    Optional<LearningGap> findByStudentIdAndTopicId(Long studentId, Long topicId);
}
