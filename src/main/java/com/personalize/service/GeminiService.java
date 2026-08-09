package com.personalize.service;

import com.personalize.ai.GeminiClient;
import com.personalize.ai.PromptTemplates;
import com.personalize.ai.AiResponseParser;
import com.personalize.dto.learning.LearningGapInterpretation;
import com.personalize.dto.learning.StudyPlanInterpretation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final GeminiClient geminiClient;
    private final AiResponseParser responseParser;

    public LearningGapInterpretation interpretGaps(String performanceSummary) {
        String prompt = String.format(PromptTemplates.LEARNING_GAP_PROMPT, performanceSummary);
        String rawResponse = geminiClient.generateContent(prompt, true);
        return responseParser.parse(rawResponse, LearningGapInterpretation.class);
    }

    public StudyPlanInterpretation generateStudyPlan(String gapsSummary, String contextMaterial) {
        String prompt = String.format(PromptTemplates.STUDY_PLAN_PROMPT, gapsSummary, contextMaterial);
        String rawResponse = geminiClient.generateContent(prompt, true);
        return responseParser.parse(rawResponse, StudyPlanInterpretation.class);
    }

    public String generateGenericContent(String prompt) {
        return geminiClient.generateContent(prompt, false);
    }
}
