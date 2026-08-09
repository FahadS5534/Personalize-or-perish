package com.personalize.controller;

import com.personalize.dto.assessment.AssessmentRequest;
import com.personalize.dto.assessment.AssessmentResponse;
import com.personalize.dto.question.QuestionRequest;
import com.personalize.dto.question.QuestionResponse;
import com.personalize.entity.User;
import com.personalize.service.AssessmentService;
import com.personalize.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<AssessmentResponse> createAssessment(@Valid @RequestBody AssessmentRequest request) {
        User facilitator = userService.getCurrentUser();
        return new ResponseEntity<>(assessmentService.createAssessment(request, facilitator), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AssessmentResponse>> getAllAssessments() {
        return ResponseEntity.ok(assessmentService.getAllAssessments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssessmentResponse> getAssessmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessmentResponseById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<AssessmentResponse> updateAssessment(
            @PathVariable Long id,
            @Valid @RequestBody AssessmentRequest request
    ) {
        User facilitator = userService.getCurrentUser();
        return ResponseEntity.ok(assessmentService.updateAssessment(id, request, facilitator));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<Void> deleteAssessment(@PathVariable Long id) {
        User facilitator = userService.getCurrentUser();
        assessmentService.deleteAssessment(id, facilitator);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/questions")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<QuestionResponse> addQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request
    ) {
        User facilitator = userService.getCurrentUser();
        return new ResponseEntity<>(assessmentService.addQuestion(id, request, facilitator), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestions(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getQuestions(id));
    }
}
