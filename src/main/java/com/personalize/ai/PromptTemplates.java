package com.personalize.ai;

public class PromptTemplates {

    public static final String LEARNING_GAP_PROMPT = """
            You are an AI learning analyst in the 'Personalize or Perish' platform.
            Analyze the student's assessment performance and explain their knowledge gaps.
            
            Student performance metrics:
            %s
            
            Please provide a structured JSON response matching the schema below.
            Do not include markdown code fences or backticks. Return ONLY pure JSON.
            
            Schema:
            {
                "summary": "Overall summary of student strengths and weaknesses",
                "priorityTopics": [
                    {
                        "topic": "Topic Name",
                        "priority": "HIGH" | "MEDIUM" | "LOW",
                        "reason": "Why this topic is prioritized, citing implications",
                        "recommendedActions": ["Action 1", "Action 2"]
                    }
                ]
            }
            """;

    public static final String STUDY_PLAN_PROMPT = """
            You are an AI tutor in the 'Personalize or Perish' platform.
            Generate a personalized daily study plan for a student based on their learning gaps.
            
            Student Learning Gaps:
            %s
            
            Study material context (if any):
            %s
            
            Please provide a structured JSON response. Return ONLY pure JSON.
            
            Schema:
            {
                "title": "Study Plan Title",
                "description": "General description and advice for the student",
                "items": [
                    {
                        "topic": "Topic Name",
                        "content": "Specific actions to take (e.g., read section, write code, run tests)",
                        "estimatedMinutes": 45,
                        "priority": "HIGH" | "MEDIUM" | "LOW"
                    }
                ]
            }
            """;
}
