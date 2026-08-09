package com.personalize.controller;

import com.personalize.dto.material.StudyMaterialRequest;
import com.personalize.dto.material.StudyMaterialResponse;
import com.personalize.service.StudyMaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-materials")
@RequiredArgsConstructor
public class StudyMaterialController {

    private final StudyMaterialService studyMaterialService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<StudyMaterialResponse> generateStudyMaterial(@Valid @RequestBody StudyMaterialRequest request) {
        return new ResponseEntity<>(studyMaterialService.generateStudyMaterial(request), HttpStatus.CREATED);
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<StudyMaterialResponse>> getMaterialsByTopic(@PathVariable Long topicId) {
        return ResponseEntity.ok(studyMaterialService.getMaterialsByTopic(topicId));
    }

    @GetMapping
    public ResponseEntity<List<StudyMaterialResponse>> getAllMaterials() {
        return ResponseEntity.ok(studyMaterialService.getAllMaterials());
    }
}
