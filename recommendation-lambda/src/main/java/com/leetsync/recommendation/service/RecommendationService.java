package com.leetsync.recommendation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leetsync.recommendation.model.ProblemRecommendation;
import com.leetsync.recommendation.model.TagRecommendation;
import com.leetsync.recommendation.model.UserRecommendations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    private static final ZoneId SEATTLE_ZONE = ZoneId.of("America/Los_Angeles");
    
    private final UserStatsService userStatsService;
    private final BedrockService bedrockService;
    private final RecommendationCacheService cacheService;
    private final ObjectMapper objectMapper;
    
    public RecommendationService() {
        this.userStatsService = new UserStatsService();
        this.bedrockService = new BedrockService();
        this.cacheService = new RecommendationCacheService();
        this.objectMapper = new ObjectMapper();
    }
    
    
    public void generateDailyRecommendations() {
        try {
            List<String> activeUsers = userStatsService.getActiveUsersFromYesterday();
            logger.info("Processing recommendations for {} active users", activeUsers.size());
            
            for (String username : activeUsers) {
                try {
                    generateRecommendationsForUser(username);
                } catch (Exception e) {
                    logger.error("Failed to generate recommendations for user: {}", username, e);
                }
            }
            
            logger.info("Completed daily recommendation generation");
            
        } catch (Exception e) {
            logger.error("Error in daily recommendation generation", e);
            throw new RuntimeException("Failed to generate daily recommendations", e);
        }
    }
    
    public UserRecommendations getRecommendationsForUser(String username) {
        UserRecommendations cached = cacheService.getRecommendations(username);
        if (cached != null) {
            logger.info("Returning cached recommendations for user: {}", username);
            return cached;
        }
        
        logger.info("Generating on-demand recommendations for user: {}", username);
        return generateRecommendationsForUser(username);
    }
    
    private UserRecommendations generateRecommendationsForUser(String username) {
        try {
            String userStatsJson = userStatsService.getUserStatsAsJson(username);
            String recommendationsJson = bedrockService.generateRecommendations(userStatsJson);
            
            Map<String, Object> recommendationsMap = objectMapper.readValue(recommendationsJson, 
                    new TypeReference<Map<String, Object>>() {});
            
            List<TagRecommendation> tagRecs = parseTagRecommendations(recommendationsMap);
            List<ProblemRecommendation> problemRecs = parseProblemRecommendations(recommendationsMap);
            
            String today = LocalDate.now(SEATTLE_ZONE).toString();
            UserRecommendations recommendations = new UserRecommendations(
                    username, today, tagRecs, problemRecs, 0L, 0L
            );
            
            cacheService.saveRecommendations(recommendations);
            logger.info("Generated and cached recommendations for user: {}", username);
            
            return recommendations;
            
        } catch (Exception e) {
            logger.error("Error generating recommendations for user: {}", username, e);
            throw new RuntimeException("Failed to generate recommendations for user: " + username, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<TagRecommendation> parseTagRecommendations(Map<String, Object> recommendationsMap) {
        List<TagRecommendation> tagRecommendations = new ArrayList<>();
        
        List<Map<String, Object>> tagList = (List<Map<String, Object>>) recommendationsMap.get("tagRecommendations");
        if (tagList != null) {
            for (Map<String, Object> tagMap : tagList) {
                TagRecommendation tagRec = new TagRecommendation(
                        (String) tagMap.get("tag"),
                        (String) tagMap.get("reason"),
                        ((Number) tagMap.get("confidenceScore")).doubleValue(),
                        ((Number) tagMap.get("estimatedDifficulty")).intValue()
                );
                tagRecommendations.add(tagRec);
            }
        }
        
        return tagRecommendations;
    }
    
    @SuppressWarnings("unchecked")
    private List<ProblemRecommendation> parseProblemRecommendations(Map<String, Object> recommendationsMap) {
        List<ProblemRecommendation> problemRecommendations = new ArrayList<>();
        
        List<Map<String, Object>> problemList = (List<Map<String, Object>>) recommendationsMap.get("problemRecommendations");
        if (problemList != null) {
            for (Map<String, Object> problemMap : problemList) {
                ProblemRecommendation problemRec = new ProblemRecommendation(
                        ((Number) problemMap.get("problemId")).intValue(),
                        (String) problemMap.get("title"),
                        (String) problemMap.get("difficulty"),
                        (List<String>) problemMap.get("tags"),
                        (String) problemMap.get("reason"),
                        ((Number) problemMap.get("matchScore")).doubleValue()
                );
                problemRecommendations.add(problemRec);
            }
        }
        
        return problemRecommendations;
    }
}