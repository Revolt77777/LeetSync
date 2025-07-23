# Microservices Architecture Refactor - TODO

## Goal: Eliminate Shared Module Dependencies
Following microservices best practices, each service should own its data models to ensure service autonomy and independent deployments.

## Current Shared Dependencies to Eliminate

### 1. **AcSubmission Model**
**Current users:**
- `ingestion-lambda` - Creates AcSubmission records
- `rest-api-server` - Reads AcSubmission records for API responses

**Action Required:**
- [ ] Move `AcSubmission.java` from `shared/` to `ingestion-lambda/src/main/java/.../model/`  
- [ ] Create API-specific `AcSubmission.java` in `rest-api-server/src/main/java/.../model/` with DynamoDB annotations
- [ ] Remove shared dependency from both services

### 2. **User Model**
**Current users:**
- `ingestion-lambda` - User management operations
- `rest-api-server` - User CRUD operations  

**Action Required:**
- [ ] Move `User.java` from `shared/` to appropriate service (likely `rest-api-server` as it handles user CRUD)
- [ ] Create service-specific User model in `ingestion-lambda` if needed
- [ ] Remove shared dependency

### 3. **Problem Model**
**Current users:**
- `problem-lambda` - Problem metadata management
- `etl-stream-lambda` - Problem data enrichment

**Action Required:**
- [ ] Move `Problem.java` from `shared/` to `problem-lambda/src/main/java/.../model/`
- [ ] Create ETL-specific Problem model in `etl-stream-lambda` if needed
- [ ] Remove shared dependency from both services

## Benefits of Elimination

✅ **Service Autonomy** - Each service controls its data models  
✅ **Independent Evolution** - Services can modify models without affecting others  
✅ **Technology Freedom** - Services can choose different persistence approaches  
✅ **Deployment Independence** - No shared dependency deployment coordination  
✅ **True Microservices** - Follows industry best practices

## Implementation Strategy

1. **Start with least-coupled models** (Problem, then AcSubmission, then User)
2. **Create service-specific models** optimized for each use case  
3. **Maintain data contracts** through DynamoDB schema (not code)
4. **Remove shared module entirely** once all models are moved

## Current Status: ✅ Stats Models Successfully Decoupled
- ✅ `stats-lambda` owns its annotated models
- ✅ `rest-api-server` owns its API-optimized annotated models  
- ✅ Services communicate via DynamoDB data, not shared code

## Target Architecture: Pure Microservices
Each service boundary:
```
service-name/
├── src/main/java/.../model/     (Service-owned models)
├── src/main/java/.../service/   (Business logic)
├── src/main/java/.../repository/ (Data access)
└── infrastructure dependencies   (DynamoDB, S3, etc.)
```

**No shared modules between services.**