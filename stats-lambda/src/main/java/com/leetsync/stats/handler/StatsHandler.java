package com.leetsync.stats.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
// Model classes imported by services
import com.leetsync.stats.service.AthenaQueryService;
import com.leetsync.stats.service.StatsCacheService;
import com.leetsync.stats.service.StatsCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.athena.model.ResultSet;
import software.amazon.awssdk.services.athena.model.Row;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Lambda handler for daily stats calculation
 * Implements the complete Daily Calculation Process from the implementation guide:
 * 
 * Step 1: Extract Yesterday's Data
 * Step 2: Calculate Daily Metrics  
 * Step 3: Update Running Totals
 * Step 4: Calculate 7-Day Stats
 * Step 5: Calculate Streaks
 */
public class StatsHandler implements RequestHandler<ScheduledEvent, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(StatsHandler.class);
    private static final String PARQUET_BUCKET_NAME = System.getenv("PARQUET_BUCKET_NAME") != null ? 
        System.getenv("PARQUET_BUCKET_NAME") : "leetsync-parquet";
    private static final ZoneId SEATTLE_ZONE = ZoneId.of("America/Los_Angeles"); // Project standard timezone
    
    private final AthenaQueryService athenaService;
    private final StatsCacheService cacheService;
    private final StatsCalculationService calculationService;
    
    public StatsHandler() {
        this.athenaService = new AthenaQueryService();
        this.cacheService = new StatsCacheService();
        this.calculationService = new StatsCalculationService(athenaService, cacheService);
    }
    
    // Constructor for testing
    public StatsHandler(AthenaQueryService athenaService, StatsCacheService cacheService, 
                       StatsCalculationService calculationService) {
        this.athenaService = athenaService;
        this.cacheService = cacheService;
        this.calculationService = calculationService;
    }
    
    @Override
    public String handleRequest(ScheduledEvent event, Context context) {
        logger.info("Starting daily stats calculation process");
        logger.info("Debug: Using parquet bucket: {}", PARQUET_BUCKET_NAME);
        
        try {
            // Ensure external table exists (created by Glue Crawler)
            athenaService.ensureTableReady(PARQUET_BUCKET_NAME);
            
            // Get all users who had submissions yesterday (using Seattle timezone to match parquet partitioning)
            ZoneId seattleZone = ZoneId.of("America/Los_Angeles");
            LocalDate yesterday = LocalDate.now(seattleZone).minusDays(1);
            ResultSet activeUsers = athenaService.getYesterdayActiveUsers(yesterday);
            
            int processedUsers = 0;
            int totalUsers = activeUsers.rows().size() - 1; // Exclude header row
            
            logger.info("Processing stats for {} users with submissions on {}", totalUsers, yesterday);
            
            // Process each user (skip first row which is header)
            for (int i = 1; i < activeUsers.rows().size(); i++) {
                Row row = activeUsers.rows().get(i);
                if (row.data().isEmpty()) continue;
                
                String username = row.data().get(0).varCharValue();
                if (username == null || username.isEmpty()) continue;
                
                try {
                    processUserStats(username, yesterday);
                    processedUsers++;
                    
                    if (processedUsers % 10 == 0) {
                        logger.info("Processed {}/{} users", processedUsers, totalUsers);
                    }
                    
                } catch (Exception e) {
                    logger.error("Failed to process stats for user: {}", username, e);
                    // Continue processing other users
                }
            }
            
            String result = String.format("Successfully processed stats for %d/%d users on %s", 
                                        processedUsers, totalUsers, yesterday);
            logger.info(result);
            return result;
            
        } catch (Exception e) {
            logger.error("Fatal error in daily stats calculation", e);
            throw new RuntimeException("Daily stats calculation failed", e);
        }
    }
    
    /**
     * Process complete stats calculation for a single user
     * Uses the new single entry point that calculates all stats efficiently
     */
    private void processUserStats(String username, LocalDate yesterday) {
        logger.debug("Processing stats for user: {} on {}", username, yesterday);
        
        // Single entry point calculates all stats (daily, total, 7-day, streaks)
        calculationService.calculateAllStatsForUser(username, yesterday);
        
        logger.debug("Completed stats processing for user: {}", username);
    }
    
    /**
     * Alternative entry point for processing a specific user (for testing/debugging)
     */
    public String processSpecificUser(String username) {
        logger.info("Processing stats for specific user: {}", username);
        
        try {
            athenaService.ensureTableReady(PARQUET_BUCKET_NAME);
            ZoneId seattleZone = ZoneId.of("America/Los_Angeles");
            LocalDate yesterday = LocalDate.now(seattleZone).minusDays(1);
            
            processUserStats(username, yesterday);
            
            String result = String.format("Successfully processed stats for user: %s on %s", username, yesterday);
            logger.info(result);
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to process stats for user: {}", username, e);
            throw new RuntimeException("User stats processing failed: " + username, e);
        }
    }
    
    /**
     * Health check entry point
     */
    public String healthCheck() {
        try {
            // Test basic connectivity
            athenaService.ensureTableReady(PARQUET_BUCKET_NAME);
            
            // Test cache connectivity by trying to fetch non-existent user (returns null safely)
            cacheService.getTotalStats("healthcheck-user");
            
            return "Stats Lambda health check passed";
            
        } catch (Exception e) {
            logger.error("Health check failed", e);
            throw new RuntimeException("Health check failed", e);
        }
    }
}