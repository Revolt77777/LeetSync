package com.leetsync.api.controller;

import com.leetsync.api.dto.DifficultyStatsResponse;
import com.leetsync.api.dto.StatsSummaryResponse;
import com.leetsync.api.dto.TagStatsResponse;
import com.leetsync.api.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * REST controller for stats endpoints
 * Provides user statistics from the stats cache
 */
@RestController
@RequestMapping("/stats")
public class StatsController {
    
    private static final Logger logger = LoggerFactory.getLogger(StatsController.class);
    private final StatsService statsService;
    
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }
    
    /**
     * GET /api/stats/{username}/summary
     * Returns combined summary stats (daily, total, streaks)
     */
    @GetMapping("/{username}/summary")
    public ResponseEntity<StatsSummaryResponse> getSummaryStats(@PathVariable String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Invalid username provided for summary stats: {}", username);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            logger.debug("Fetching summary stats for user: {}", username);
            Optional<StatsSummaryResponse> stats = statsService.getSummaryStats(username);
            
            if (stats.isPresent()) {
                return ResponseEntity.ok(stats.get());
            } else {
                logger.debug("No stats found for user: {}", username);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error getting summary stats for user: {}", username, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/stats/{username}/difficulty  
     * Returns difficulty breakdown for yesterday and total
     */
    @GetMapping("/{username}/difficulty")
    public ResponseEntity<DifficultyStatsResponse> getDifficultyStats(@PathVariable String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Invalid username provided for difficulty stats: {}", username);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            logger.debug("Fetching difficulty stats for user: {}", username);
            Optional<DifficultyStatsResponse> stats = statsService.getDifficultyStats(username);
            
            if (stats.isPresent()) {
                return ResponseEntity.ok(stats.get());
            } else {
                logger.debug("No difficulty stats found for user: {}", username);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error getting difficulty stats for user: {}", username, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/stats/{username}/tags
     * Returns tag performance metrics from total stats  
     */
    @GetMapping("/{username}/tag")
    public ResponseEntity<TagStatsResponse> getTagStats(@PathVariable String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Invalid username provided for tag stats: {}", username);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            logger.debug("Fetching tag stats for user: {}", username);
            Optional<TagStatsResponse> stats = statsService.getTagStats(username);
            
            if (stats.isPresent()) {
                return ResponseEntity.ok(stats.get());
            } else {
                logger.debug("No tag stats found for user: {}", username);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error getting tag stats for user: {}", username, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}