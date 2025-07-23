package com.leetsync.recommendation.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.leetsync.recommendation.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecommendationHandler implements RequestHandler<ScheduledEvent, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationHandler.class);
    private final RecommendationService recommendationService;
    
    public RecommendationHandler() {
        this.recommendationService = new RecommendationService();
    }
    
    public RecommendationHandler(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }
    
    @Override
    public String handleRequest(ScheduledEvent event, Context context) {
        logger.info("Starting daily recommendation generation");
        
        try {
            recommendationService.generateDailyRecommendations();
            
            String response = "Daily recommendations generated successfully";
            logger.info(response);
            return response;
            
        } catch (Exception e) {
            logger.error("Error in recommendation handler", e);
            throw new RuntimeException("Failed to generate recommendations", e);
        }
    }
}