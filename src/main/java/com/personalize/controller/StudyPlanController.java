package com.personalize.controller;

import com.personalize.dto.learning.StudyPlanResponse;
import com.personalize.entity.Role;
import com.personalize.entity.User;
import com.personalize.exception.BadRequestException;
import com.personalize.service.StudyPlanService;
import com.personalize.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;
    private final UserService userService;

    @PostMapping("/generate/{studentId}")
    public ResponseEntity<StudyPlanResponse> generateStudyPlan(@PathVariable Long studentId) {
        User currentUser = userService.getCurrentUser();

        // Safety check: student can only trigger plan generation for themselves
        if (currentUser.getRole() == Role.STUDENT && !currentUser.getId().equals(studentId)) {
            throw new BadRequestException("You can only generate study plans for yourself");
        }

        return new ResponseEntity<>(studyPlanService.generateStudyPlan(studentId), HttpStatus.CREATED);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudyPlanResponse>> getStudentStudyPlans(@PathVariable Long studentId) {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() == Role.STUDENT && !currentUser.getId().equals(studentId)) {
            throw new BadRequestException("You can only query your own study plans");
        }

        return ResponseEntity.ok(studyPlanService.getStudentStudyPlans(studentId));
    }

    @GetMapping("/student/{studentId}/latest")
    public ResponseEntity<StudyPlanResponse> getLatestStudyPlan(@PathVariable Long studentId) {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() == Role.STUDENT && !currentUser.getId().equals(studentId)) {
            throw new BadRequestException("You can only query your own study plans");
        }

        return ResponseEntity.ok(studyPlanService.getLatestStudyPlan(studentId));
    }

    @PutMapping("/{planId}/items/{itemId}")
    public ResponseEntity<StudyPlanResponse> toggleItemCompleted(
            @PathVariable Long planId,
            @PathVariable Long itemId,
            @RequestParam Boolean completed
    ) {
        User currentUser = userService.getCurrentUser();
        // Since study plan item modification is student specific, verify the logged in user is the owner
        // Retrieve plan first to check student ID
        StudyPlanResponse plan = studyPlanService.toggleItemCompleted(planId, itemId, completed);
        if (currentUser.getRole() == Role.STUDENT && !currentUser.getId().equals(plan.getStudentId())) {
            throw new BadRequestException("You can only edit items in your own study plan");
        }
        return ResponseEntity.ok(plan);
    }
}
