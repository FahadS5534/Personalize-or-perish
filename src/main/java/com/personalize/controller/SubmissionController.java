package com.personalize.controller;

import com.personalize.dto.submission.SubmissionRequest;
import com.personalize.dto.submission.SubmissionResponse;
import com.personalize.entity.User;
import com.personalize.service.SubmissionService;
import com.personalize.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionResponse> submitAssessment(@Valid @RequestBody SubmissionRequest request) {
        User student = userService.getCurrentUser();
        return new ResponseEntity<>(submissionService.submitAssessment(request, student), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> getSubmissionById(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(submissionService.getSubmissionDetails(id, user));
    }

    @GetMapping
    public ResponseEntity<List<SubmissionResponse>> getSubmissions(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long assessmentId
    ) {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(submissionService.getSubmissions(studentId, assessmentId, user));
    }
}
