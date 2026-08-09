package com.personalize.controller;

import com.personalize.dto.learning.LearningGapResponse;
import com.personalize.entity.Role;
import com.personalize.entity.User;
import com.personalize.exception.BadRequestException;
import com.personalize.service.LearningGapService;
import com.personalize.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/learning-gaps")
@RequiredArgsConstructor
public class LearningGapController {

    private final LearningGapService learningGapService;
    private final UserService userService;

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<LearningGapResponse>> getStudentLearningGaps(@PathVariable Long studentId) {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() == Role.STUDENT && !currentUser.getId().equals(studentId)) {
            throw new BadRequestException("You can only query your own learning gaps");
        }

        return ResponseEntity.ok(learningGapService.getStudentLearningGaps(studentId));
    }
}
