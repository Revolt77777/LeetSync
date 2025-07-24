# LeetSync Deploy-Test-Promote Pipeline

Modern CI/CD pipeline that deploys to a test environment first, runs real integration tests against live AWS services, then promotes to production only if tests pass.

## 🚀 Pipeline Flow

```
┌───────────┐    ┌─────────────┐    ┌────────────────┐    ┌─────────────┐
│   Unit    │ →  │   Deploy    │ →  │  Integration   │ →  │   Deploy    │
│   Tests   │    │    Test     │    │     Tests      │    │  Production │
│           │    │             │    │  (Real AWS)    │    │             │
└───────────┘    └─────────────┘    └────────────────┘    └─────────────┘
```

### What Makes This Modern?
- **Deploy first, test second** - Tests run against real AWS infrastructure
- **Real integration tests** - No mocking of AWS services
- **Fail fast** - Production deployment blocked if integration tests fail
- **Health-check gates** - Verify deployments are actually working

## 📋 Required Setup

### 1. AWS IAM Roles

Create **three** IAM roles for GitHub Actions:

#### Test Environment Role
```bash
# Role Name: GitHubActionsRole-Test
# Purpose: Deploy and test in test environment
# Permissions: Full access to deploy LeetSync test resources
```

#### Production Role
```bash
# Role Name: GitHubActionsRole-Prod  
# Purpose: Deploy to production environment
# Permissions: Full access to deploy LeetSync production resources
```

### 2. GitHub Repository Secrets

Add these secrets in `Settings → Secrets and variables → Actions`:

| Secret Name | Description | Example Value |
|-------------|-------------|---------------|
| `AWS_ROLE_ARN_TEST` | Test environment deployment role | `arn:aws:iam::123456789:role/GitHubActionsRole-Test` |
| `AWS_ROLE_ARN_PROD` | Production deployment role | `arn:aws:iam::123456789:role/GitHubActionsRole-Prod` |

### 3. GitHub Environments

Create environments in `Settings → Environments`:

- **`test`** - Auto-deploys, used for integration testing
- **`production`** - Manual approval required, for live users

## 🎯 Pipeline Stages

### Stage 1: Validate (2 minutes)
```yaml
✅ Unit tests with mocks
✅ Build all JAR files  
✅ Upload artifacts for next stages
```

### Stage 2: Deploy to Test (3 minutes)
```yaml
✅ Deploy all infrastructure to AWS test environment
✅ Create real DynamoDB tables, Lambda functions, S3 buckets
✅ Extract API Gateway URL for testing
```

### Stage 3: Integration Tests (5 minutes)
```yaml
✅ Test AWS infrastructure health (DynamoDB, Lambda, S3)
✅ Test API endpoints with real HTTP calls
✅ Test LeetCode API integration with real requests
✅ Test data pipeline flow with real data
✅ Light performance testing
```

### Stage 4: Deploy to Production (3 minutes)
```yaml
✅ Only runs if integration tests pass
✅ Deploy to production AWS environment  
✅ Run health checks against production
✅ Verify all services are operational
```

### Stage 5: Summary
```yaml
✅ Generate deployment report
✅ Show test results and production status
✅ Create GitHub summary with links and status
```

## 🔄 Workflow Triggers

### Push to `main` branch:
```
Unit Tests → Deploy Test → Integration Tests → Deploy Production
```

### Push to `develop` branch:
```  
Unit Tests → Deploy Test → Integration Tests (Production skipped)
```

### Pull Request to `main`:
```
Unit Tests only (No deployment)
```

## 🧪 Integration Tests Explained

Unlike unit tests that use mocks, integration tests run against **real AWS services**:

### AWS Infrastructure Tests
```bash
# Test real DynamoDB tables exist and are accessible
aws dynamodb describe-table --table-name LeetSyncUsers-test

# Test real Lambda functions are deployed and ready
aws lambda get-function --function-name LeetSyncIngestion-test

# Test real S3 bucket exists
aws s3 ls s3://leetsync-parquet-test/
```

### API Endpoint Tests
```bash
# Test real API Gateway endpoints
curl -f "https://test-api.leetsync.com/users"

# Test real user creation flow
curl -X POST "https://test-api.leetsync.com/users/testuser"
```

### LeetCode Integration Tests
```bash
# Test real LeetCode API calls (not mocked)
aws lambda invoke --function-name LeetSyncIngestion-test
```

### Data Pipeline Tests
```bash
# Test real end-to-end data flow
# Submission → DynamoDB → Streams → ETL → S3 → Athena → Stats
aws dynamodb scan --table-name LeetSyncSubmissions-test
```

## 💰 Cost Impact

For **5 users**:
- **Test environment**: ~$1/month
- **Production environment**: ~$1/month  
- **Total**: ~$2/month (mostly AWS free tier)

The real cost is **GitHub Actions minutes**, but you get 2,000 free minutes/month.

## 🔍 Monitoring & Debugging

### View Pipeline Results
1. Go to **Actions** tab in GitHub
2. Click on workflow run to see each stage
3. Expand stages to see detailed logs

### Common Issues

#### Integration Tests Fail
```
✅ Check AWS credentials are correctly configured
✅ Verify IAM roles have necessary permissions  
✅ Check if AWS services are experiencing outages
✅ Look at specific test failure logs
```

#### Deployment Fails
```
✅ Check CDK synthesis works locally: `cdk synth`
✅ Verify AWS account has necessary quotas
✅ Check CloudFormation logs in AWS console
✅ Ensure all required environment variables are set
```

#### API Tests Timeout
```
✅ Check API Gateway URL is correct
✅ Verify Lambda functions are not cold starting
✅ Check VPC/security group configurations
✅ Increase timeout values if needed
```

## 🏆 Benefits of This Approach

### Catches Real Issues
```
❌ Unit tests: Mock LeetCode API → Misses rate limiting
✅ Integration tests: Real LeetCode API → Catches rate limiting

❌ Unit tests: Mock DynamoDB → Misses scaling issues  
✅ Integration tests: Real DynamoDB → Catches throttling

❌ Unit tests: Mock Athena → Misses query performance
✅ Integration tests: Real Athena → Catches slow queries
```

### Production Confidence
```
✅ If integration tests pass → Production will likely work
✅ Real AWS services tested → No "works on my machine"
✅ API endpoints verified → Users won't see broken features
✅ Data pipeline validated → Analytics will work correctly
```

### Modern Best Practices
```
✅ Deploy-first approach → Industry standard at Netflix, Spotify
✅ Real integration testing → Better than mocking everything
✅ Automated promotion → No manual deployment errors
✅ Health-check gates → Safer production deployments
```

## 🚀 Getting Started

1. **Set up AWS IAM roles** with GitHub OIDC trust policies
2. **Add GitHub secrets** for the role ARNs
3. **Create GitHub environments** for approval workflows
4. **Push to develop branch** to test the pipeline
5. **Push to main branch** to deploy to production

Your modern CI/CD pipeline will:
- ✅ Deploy to test environment in **3 minutes**  
- ✅ Run real integration tests in **5 minutes**
- ✅ Deploy to production in **3 minutes**
- ✅ Total time: **~11 minutes** from commit to production

This gives you **Netflix-level CI/CD** for your personal project! 🎉