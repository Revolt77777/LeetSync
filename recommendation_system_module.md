# LeetSync Recommendation System Module

## Overview
AWS Bedrock Claude 3.5 Sonnet integration for generating personalized tag and problem recommendations based on UserStatsCache data.

## Cost Analysis (Daily Batch + 24h Cache)

| Users | Bedrock API | Infrastructure | **Total Monthly** |
|-------|-------------|----------------|-------------------|
| 1 | $0.63 | $0.03 | **$0.66** |
| 10 | $4.53 | $0.05 | **$4.58** |
| 50 | $21.80 | $0.08 | **$21.88** |
| 100 | $43.40 | $0.12 | **$43.52** |

**Bedrock Pricing:** $3/M input tokens, $15/M output tokens

## Architecture Components

### Data Sources
- **UserStatsCache table**: TotalStats, DailyStats, StreakStats per user

### New Components
- **RecommendationsCache table**: Store daily recommendations with 24h TTL
- **RecommendationService lambda**: Daily batch processing (6 AM PT)
- **REST API endpoints**: Get cached recommendations

## Data Flow
1. Daily lambda queries UserStatsCache for all active users
2. Batch API call to Bedrock Claude 3.5 Sonnet
3. Parse and store recommendations in cache with TTL
4. API serves cached data, fallback to on-demand generation

## Token Usage (Batch Optimized)
- **Input per batch**: 2,200 + (800 × N users) tokens
- **Output per user**: 800 tokens (tags + problems + reasoning)
- **Batch savings**: ~15-20% vs individual calls

## Recommendations Output
- **Tags**: 5-7 algorithmic focus areas with reasoning
- **Problems**: 5-10 specific next problems with match scores

---
**Status**: Ready for implementation
**Testing**: Start with 1 user ($0.66/month)
**Scale**: Supports 1-100 users efficiently