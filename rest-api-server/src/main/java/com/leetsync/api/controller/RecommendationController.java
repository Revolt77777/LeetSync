package com.leetsync.api.controller;

import com.leetsync.api.dto.RecommendationsResponse;
import com.leetsync.api.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<RecommendationsResponse> getRecommendations(@PathVariable String username) {
        return recommendationService.getRecommendations(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}