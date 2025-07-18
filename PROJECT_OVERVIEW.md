# LeetSync Project Overview

## Vision

"Your coding-interview fitness tracker." Automates collection, storage, and visualization of all LeetCode submissions
for user zxuanxu, turning raw practice into actionable analytics.

## Current Architecture (Updated)

Multi-stack serverless AWS deployment with Java CDK:

### CDK Stacks

1. **LeetSyncDataStack** - DynamoDB AcSubmissions table (id: NUMBER PK)
2. **LeetSyncIngestionStack** - Daily ingestion lambda + EventBridge cron (6 AM UTC)
3. **LeetSyncApiStack** - REST API lambda + HTTP API Gateway

### Components

- **Ingestion Lambda**: Fetches 20 recent submissions via LeetCode GraphQL → DynamoDB (idempotent writes)
- **API Lambda**: Spring Boot HTTP handler for querying submission data
- **Shared Model**: Common POJOs across all modules

## Project Structure

```
leetsync/
├─ shared/                    # Shared model library
├─ ingestion-lambda/          # Daily sync handler
├─ rest-api-server/           # Spring Boot API
├─ infrastructure/            # Java CDK stacks
└─ pom.xml                    # Maven multi-module
```

## Current Status

✅ Multi-stack CDK architecture implemented
✅ Ingestion lambda with daily scheduling
✅ API lambda with HTTP Gateway
✅ Shared DynamoDB table with proper IAM permissions
🔄 Ready for deployment testing

## Deployment

```bash
mvn clean package
cd infrastructure
cdk deploy LeetSyncDataStack
cdk deploy LeetSyncIngestionStack LeetSyncApiStack
```

## Next Phase (Blueprint Alignment)

- Add stats-lambda for nightly roll-ups
- Add aiassist-lambda for RAG recommendations
- Implement Insights table for analytics
- Add S3 data lake + Firehose
- Convert to TypeScript CDK (infra-cdk/)
- Add GitHub Actions CI/CD

## Tech Stack

- **Runtime**: Java 21
- **Framework**: Spring Boot 3.2.6 (API only), plain Lambda handlers (ingestion)
- **Infrastructure**: AWS CDK (Java), DynamoDB, Lambda, EventBridge, API Gateway
- **Build**: Maven multi-module
- **Testing**: JUnit 5, Mockito

## Development Notes

- User hardcoded: zxuanxu
- Fetch limit: 20 submissions per run
- Environment variables: TABLE_NAME, LOG_LEVEL
- IAM: Least privilege (read/write separation)
- Data model: Single table with numeric ID partition key

## Key Implementation Details

- **Idempotent writes**: `attribute_not_exists(id)` condition prevents duplicates
- **Handler signature**: `RequestHandler<Void, String>` for EventBridge triggers
- **Dependencies**: AWS SDK v2, Jackson for JSON, SLF4J for logging
- **Build artifacts**: Shaded JARs for Lambda deployment
