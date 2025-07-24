# LeetSync

A comprehensive microservices platform for tracking, analyzing, and optimizing LeetCode solving progress with AI-powered recommendations.

## Overview

LeetSync is a cloud-native analytics platform that helps developers track their LeetCode progress, gain insights into their coding patterns, and receive personalized recommendations for skill improvement. The system automatically syncs submission data from LeetCode, processes it through a sophisticated analytics pipeline, and provides actionable insights through both REST APIs and AI-generated recommendations.

## Architecture

### System Design

LeetSync follows a **microservices architecture** deployed on AWS, with each service responsible for a specific domain

### Core Components

#### 1. **Data Ingestion** (`ingestion-lambda/`)
- **Purpose**: Syncs user submission data from LeetCode
- **Pattern**: Scheduled Lambda with external API integration
- **Features**:
  - GraphQL integration with LeetCode API
  - Robust parsing of submission metadata (runtime, memory)
  - Duplicate prevention with conditional DynamoDB writes
  - Error handling with graceful user-level failures

#### 2. **Problem Management** (`problem-lambda/`)
- **Purpose**: Maintains comprehensive LeetCode problem database
- **Pattern**: Batch processing with dual API integration
- **Features**:
  - REST API for bulk problem data
  - GraphQL API for detailed topic tags
  - Batch optimization (100 items per DynamoDB batch)
  - Incremental updates to avoid unnecessary writes

#### 3. **ETL Stream Processing** (`etl-stream-lambda/`)
- **Purpose**: Real-time data transformation for analytics
- **Pattern**: Event-driven processing with DynamoDB Streams
- **Features**:
  - Parquet file generation for columnar analytics
  - Hive-style partitioning by date
  - S3 data lake integration
  - Problem metadata enrichment

#### 4. **Analytics Engine** (`stats-lambda/`)
- **Purpose**: Complex statistical analysis and metric computation
- **Pattern**: Serverless analytics with AWS Athena integration
- **Features**:
  - Advanced streak calculation algorithms
  - Difficulty distribution analysis
  - Tag-based performance metrics
  - Running averages and weighted calculations
  - Memory-efficient single-query processing

#### 5. **AI Recommendations** (`recommendation-lambda/`)
- **Purpose**: Personalized learning recommendations using AI
- **Pattern**: AI service orchestration with caching
- **Features**:
  - AWS Bedrock Claude 3.5 Sonnet integration
  - Structured prompt engineering for consistent outputs
  - Multi-service data aggregation (stats → AI → cache)
  - 24-hour TTL caching

#### 6. **REST API Server** (`rest-api-server/`)
- **Purpose**: Public API for web and mobile applications
- **Pattern**: Spring Boot microservice with repository pattern
- **Features**:
  - RESTful endpoints for all data domains
  - DTO transformation layer
  - Comprehensive error handling
  - Data aggregation from multiple sources

#### 7. **Infrastructure as Code** (`infrastructure/`)
- **Purpose**: AWS CDK infrastructure definitions
- **Pattern**: Stack-based resource organization
- **Features**:
  - Modular stack design for each service domain
  - Cross-stack resource sharing
  - Environment-specific configurations

## Data Models

### Core Entities

#### User Model
```java
class User {
    String username;        // Primary key
    long lastSyncTimestamp; // Last data sync time
}
```

#### Submission Model
```java
class AcSubmission {
    String username;        // Partition key
    long timestamp;         // Sort key
    String titleSlug;       // Problem identifier
    int runtime;           // Execution time in ms
    int memory;            // Memory usage in MB
    String difficulty;     // Easy/Medium/Hard
}
```

#### Problem Model
```java
class Problem {
    String titleSlug;      // Primary key
    String title;          // Display name
    int difficulty;        // 1=Easy, 2=Medium, 3=Hard
    double acRate;         // Acceptance rate
    List<TopicTag> topicTags; // Categories/algorithms
}
```

### Statistics Models

LeetSync uses a **single-table design** for statistics with composite keys:

- **Partition Key**: `username`
- **Sort Key**: `statType` (TOTAL, STREAKS, DAILY#YYYY-MM-DD)

#### TotalStats
```java
class TotalStats {
    DifficultyBreakdown difficulty;     // Easy/Medium/Hard counts and percentages
    Map<String, TagAverage> tags;       // Performance by algorithm tag
    int totalSolved;                    // Lifetime problem count
    double averageRuntime;              // Performance metrics
}
```

#### DailyStats
```java
class DailyStats {
    String date;           // YYYY-MM-DD format
    int problemsSolved;    // Daily count
    DifficultyBreakdown difficulty; // Daily breakdown
    long ttl;             // Auto-cleanup timestamp
}
```

#### StreakStats
```java
class StreakStats {
    int currentStreak;     // Active solving streak
    int longestStreak;     // Historical maximum
    String lastSolvedDate; // Last activity date
}
```

### AI Recommendation Models

#### UserRecommendations
```java
class UserRecommendations {
    String username;
    List<ProblemRecommendation> problems;  // Specific problem suggestions
    List<TagRecommendation> tags;          // Topic area recommendations
    String reasoning;                      // AI explanation
    long ttl;                             // 24-hour cache expiration
}
```

## API Endpoints

### User Management
- `GET /api/users` - List all users
- `GET /api/users/{username}` - Get user details
- `POST /api/users/{username}` - Create new user
- `DELETE /api/users/{username}` - Remove user (cascade delete)

### Statistics
- `GET /api/stats/{username}/summary` - Combined overview (daily + total + streaks)
- `GET /api/stats/{username}/difficulty` - Difficulty breakdown analysis
- `GET /api/stats/{username}/tag` - Algorithm tag performance metrics

### Recommendations
- `GET /api/recommendations/{username}` - AI-powered learning suggestions

## Technology Stack

### Backend Services
- **Java 21** - Modern JVM with performance optimizations
- **Spring Boot 3.2.6** - Microservices framework (REST API only)
- **AWS Lambda** - Serverless compute for event-driven processing
- **Maven** - Multi-module project management

### Data Layer
- **Amazon DynamoDB** - NoSQL database with single-table design
- **Amazon S3** - Data lake for analytics (Parquet format)
- **AWS Athena** - Serverless analytics query engine

### AI/ML Services
- **AWS Bedrock** - Managed AI service
- **Claude 3.5 Sonnet** - Large language model for recommendations

### Infrastructure
- **AWS CDK** - Infrastructure as Code
- **DynamoDB Streams** - Real-time change data capture
- **EventBridge** - Scheduled Lambda triggers

### External Integrations
- **LeetCode REST API** - Problem metadata
- **LeetCode GraphQL API** - User submissions and detailed data

## Design Patterns

### 1. **Microservices Architecture**
- **Service Autonomy**: Each service owns its data models and business logic
- **Loose Coupling**: Services communicate through events and APIs
- **Independent Deployment**: Services can be updated independently

### 2. **Event-Driven Processing**
- **DynamoDB Streams**: Real-time ETL processing
- **EventBridge Scheduling**: Automated data synchronization
- **Lambda Triggers**: Serverless event handlers

### 3. **Single-Table Design** (DynamoDB)
- **Composite Keys**: Efficient access patterns with partition + sort keys
- **Data Locality**: Related data stored together for fast queries
- **Query Optimization**: Minimal round trips to database

### 4. **Repository Pattern**
- **Data Access Abstraction**: Clean separation between business logic and data access
- **Multiple Implementations**: Spring Data for REST API, AWS SDK for Lambdas
- **Testability**: Easy mocking for unit tests

### 5. **DTO Transformation**
- **API Contracts**: Stable external interfaces independent of internal models
- **Data Aggregation**: Combine multiple domain objects into client-friendly responses
- **Versioning**: Evolution of APIs without breaking changes

### 6. **Command Query Responsibility Segregation (CQRS)**
- **Write Path**: Lambda functions handle data ingestion and processing
- **Read Path**: REST API optimized for query performance
- **Analytics Path**: Separate ETL pipeline for complex analytics

### 7. **Circuit Breaker Pattern**
- **External API Resilience**: Graceful handling of LeetCode API failures
- **Service Isolation**: Failures in one service don't cascade
- **Timeout Management**: Configured timeouts for all external calls

## Data Flow

### 1. **Ingestion Flow**
```
LeetCode API → Ingestion Lambda → DynamoDB (AcSubmissions) → DynamoDB Streams
```

### 2. **Analytics Flow**
```
DynamoDB Streams → ETL Lambda → S3 (Parquet) → Athena → Stats Lambda → DynamoDB (UserStats)
```

### 3. **Recommendation Flow**
```
UserStats → Recommendation Lambda → AWS Bedrock → DynamoDB (RecommendationCache)
```

### 4. **API Flow**
```
Client → REST API → DynamoDB (UserStats/Cache) → Response DTOs
```

## Key Features

### ✅ **Automated Data Synchronization**
- Scheduled ingestion of user submissions from LeetCode
- Incremental updates to minimize API calls
- Robust error handling and retry logic

### ✅ **Real-time Analytics Pipeline**
- Event-driven ETL processing with DynamoDB Streams
- Columnar storage in Parquet format for efficient analytics
- Single-query optimization for complex statistical calculations

### ✅ **AI-Powered Recommendations**
- Integration with AWS Bedrock for intelligent suggestions
- Context-aware recommendations based on solving patterns
- Structured prompt engineering for consistent outputs

### ✅ **Performance Optimization**
- Single-table design for efficient DynamoDB access patterns
- Batch operations to minimize database round trips
- Caching strategies with TTL for computed results

### ✅ **Comprehensive Statistics**
- Difficulty distribution analysis
- Algorithm tag performance tracking
- Streak calculation with historical trends
- Running averages and performance metrics

### ✅ **Scalable Architecture**
- Serverless computing with automatic scaling
- Event-driven processing for loose coupling
- Infrastructure as Code for reproducible deployments

## Development

### Prerequisites
- Java 21+
- Maven 3.8+
- AWS CLI configured with appropriate permissions
- AWS CDK CLI

### Project Structure
```
leetsync/
├── pom.xml                 # Root Maven configuration
├── ingestion-lambda/       # User submission sync service
├── problem-lambda/         # LeetCode problem database service
├── etl-stream-lambda/      # Real-time data transformation
├── stats-lambda/           # Analytics and statistics engine
├── recommendation-lambda/  # AI-powered recommendations
├── rest-api-server/       # Public REST API
└── infrastructure/        # AWS CDK infrastructure definitions
```

## Monitoring and Observability

### Logging Strategy
- **Structured Logging**: SLF4J with JSON formatting
- **Context Correlation**: Request tracing across services
- **Error Classification**: Different log levels for operational vs development issues

### Metrics and Monitoring
- **CloudWatch Metrics**: Lambda execution metrics, DynamoDB performance
- **CloudWatch Alarms**: Error rate and latency thresholds
- **X-Ray Tracing**: Distributed request tracing across services

### Cost Optimization
- **TTL-based Cleanup**: Automatic removal of stale data
- **Batch Processing**: Minimize DynamoDB and Lambda invocation costs
- **S3 Lifecycle Policies**: Automatic data archival for cost management

## Security

### Data Protection
- **Encryption at Rest**: DynamoDB and S3 encryption enabled
- **Encryption in Transit**: HTTPS/TLS for all API communications
- **Access Control**: IAM roles with least privilege principles

### API Security
- **Input Validation**: Comprehensive parameter validation
- **Error Handling**: Secure error messages without data leakage
- **Rate Limiting**: Protection against abuse and excessive usage

## Future Enhancements

### 🔄 **Planned Features**
- Real-time notifications for achievements and milestones
- Social features for comparing progress with peers
- Advanced visualization dashboards
