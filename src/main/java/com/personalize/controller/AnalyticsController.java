package com.personalize.controller;

import com.personalize.dto.analytics.FacilitatorAnalyticsResponse;
import com.personalize.dto.analytics.StudentAnalyticsResponse;
import com.personalize.entity.Role;
import com.personalize.entity.User;
import com.personalize.exception.BadRequestException;
import com.personalize.service.AnalyticsService;
import com.personalize.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserService userService;

    @GetMapping("/student/{studentId}")
    public ResponseEntity<StudentAnalyticsResponse> getStudentAnalytics(@PathVariable Long studentId) {
        User currentUser = userService.getCurrentUser();
        
        if (currentUser.getRole() == Role.STUDENT && !currentUser.getId().equals(studentId)) {
            throw new BadRequestException("You can only query your own analytics");
        }
        
        return ResponseEntity.ok(analyticsService.getStudentAnalytics(studentId));
    }

    @GetMapping("/assessment/{assessmentId}")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<FacilitatorAnalyticsResponse> getAssessmentAnalytics(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(analyticsService.getAssessmentAnalytics(assessmentId));
    }
}
