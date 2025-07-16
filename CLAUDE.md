# LeetSync Project Context

## Vision
"Your coding-interview fitness tracker." Automates collection, storage, and visualization of all LeetCode submissions for the hard-coded user zxuanxu, turning raw practice into actionable analytics.

## Architecture
Serverless, event-driven AWS stack:
- **Lambda (Java 21 + Spring Boot)**: Cron via EventBridge every 5 min → fetch GraphQL API → normalize JSON
- **DynamoDB AcSubmissions table** (PK id long). Idempotent upserts prevent duplicates
- **REST API Server** (separate Spring sub-module) deployed as Lambda-powered HTTP API for querying aggregated stats
- **Notion Sync Lambda**: Consumes API, updates Notion database for live dashboards
- **IaC with AWS CDK (Java)**: Single mono-repo, Maven multi-module, shared model library

## Current Status
✔ Phase 1 ingestion stable — daily runs < 500 ms avg
✔ REST API packaged; resolving Lambda container-handler class-cast edge case
🛠 Refactor underway to replace Spring in ingestion Lambda (reduce package size) while keeping DI in API module

## Next Priorities
- Add Problem-Set tagging
- Rolling 7-day heat-map
- BigQuery export for ad-hoc SQL
- Resolve Lambda container-handler class-cast edge case

## Engineering Practices
- Test-Driven Development end-to-end (JUnit 5, Mockito inline mocks)
- Cold-start optimization experiments (native Spring AOT vs. plain Java handler)
- Observability: CloudWatch structured logs; custom logger wraps AWS request IDs for trace-level correlation
- Security & IAM: Principle-of-least-privilege roles; automated policy synthesis in CDK

## Tech Stack
- **Languages**: Java 17/21, TypeScript/JavaScript
- **Cloud & Backend**: AWS (Lambda, DynamoDB, EventBridge, CDK, SAM), Spring Boot, REST, Maven
- **Testing**: JUnit 5, Mockito
- **DevOps**: Git, GitHub Actions, AWS CDK

## Project Structure
- Mono-repo with Maven multi-module setup
- Shared model library across modules
- Separate ingestion and REST API lambdas
- Infrastructure as Code with AWS CDK

## Development Notes
- User is zxuanxu (hardcoded for personal use)
- Focus on serverless performance and cost optimization
- Part of job search portfolio for 2026 New-Grad SDE positions
- Demonstrates AWS serverless mastery and modern Java development