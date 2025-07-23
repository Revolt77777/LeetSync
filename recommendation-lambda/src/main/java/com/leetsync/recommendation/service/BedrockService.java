package com.leetsync.recommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.Map;

public class BedrockService {
    private static final Logger logger = LoggerFactory.getLogger(BedrockService.class);
    private static final String MODEL_ID = "anthropic.claude-3-5-sonnet-20241022-v2:0";
    
    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper;
    
    public BedrockService() {
        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.US_WEST_2)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    
    public String generateRecommendations(String userStatsData) {
        try {
            String prompt = buildPrompt(userStatsData);
            String requestBody = buildClaudeRequest(prompt);
            
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(MODEL_ID)
                    .body(SdkBytes.fromUtf8String(requestBody))
                    .build();
            
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();
            
            return extractContentFromResponse(responseBody);
            
        } catch (Exception e) {
            logger.error("Error generating recommendations via Bedrock", e);
            throw new RuntimeException("Failed to generate recommendations", e);
        }
    }
    
    private String buildPrompt(String userStatsData) {
        return """
            You are an expert LeetCode problem recommendation system. Based on user statistics, recommend:
            
            1. TAGS: 5-7 algorithmic tags the user should focus on
            2. PROBLEMS: 5-10 specific problems to solve next
            
            USER STATISTICS:
            %s
            
            Provide JSON response in this exact format:
            {
              "tagRecommendations": [
                {
                  "tag": "dynamic-programming",
                  "reason": "Only 23%% success rate, improvement needed",
                  "confidenceScore": 0.85,
                  "estimatedDifficulty": 2
                }
              ],
              "problemRecommendations": [
                {
                  "problemId": 70,
                  "title": "Climbing Stairs",
                  "difficulty": "Easy",
                  "tags": ["dynamic-programming"],
                  "reason": "Foundational DP problem to improve weak area",
                  "matchScore": 0.92
                }
              ]
            }
            """.formatted(userStatsData);
    }
    
    private String buildClaudeRequest(String prompt) throws Exception {
        Map<String, Object> requestMap = Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", 4000,
                "messages", new Object[]{
                        Map.of("role", "user", "content", prompt)
                }
        );
        
        return objectMapper.writeValueAsString(requestMap);
    }
    
    private String extractContentFromResponse(String responseBody) throws Exception {
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode content = responseJson.path("content").get(0).path("text");
        return content.asText();
    }
}