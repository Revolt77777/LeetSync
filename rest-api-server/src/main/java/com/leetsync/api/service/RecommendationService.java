package com.leetsync.api.service;

import com.leetsync.api.dto.ProblemRecommendationResponse;
import com.leetsync.api.dto.RecommendationsResponse;
import com.leetsync.api.dto.TagRecommendationResponse;
import com.leetsync.api.model.ProblemRecommendation;
import com.leetsync.api.model.TagRecommendation;
import com.leetsync.api.model.UserRecommendations;
import com.leetsync.api.repository.RecommendationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecommendationService {
    
    private final RecommendationRepository recommendationRepository;
    
    public RecommendationService(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }
    
    public Optional<RecommendationsResponse> getRecommendations(String username) {
        return recommendationRepository.getRecommendations(username)
                .map(this::convertToResponse);
    }
    
    private RecommendationsResponse convertToResponse(UserRecommendations userRecommendations) {
        List<TagRecommendationResponse> tagResponses = userRecommendations.getTagRecommendations().stream()
                .map(this::convertTagToResponse)
                .toList();
        
        List<ProblemRecommendationResponse> problemResponses = userRecommendations.getProblemRecommendations().stream()
                .map(this::convertProblemToResponse)
                .toList();
        
        return new RecommendationsResponse(
                userRecommendations.getUsername(),
                userRecommendations.getDate(),
                tagResponses,
                problemResponses,
                userRecommendations.getGeneratedAt()
        );
    }
    
    private TagRecommendationResponse convertTagToResponse(TagRecommendation tag) {
        return new TagRecommendationResponse(
                tag.getTag(),
                tag.getReason(),
                tag.getConfidenceScore(),
                tag.getEstimatedDifficulty()
        );
    }
    
    private ProblemRecommendationResponse convertProblemToResponse(ProblemRecommendation problem) {
        return new ProblemRecommendationResponse(
                problem.getProblemId(),
                problem.getTitle(),
                problem.getDifficulty(),
                problem.getTags(),
                problem.getReason(),
                problem.getMatchScore()
        );
    }
}