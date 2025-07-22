# LeetSync Timezone Standard

## **Project Rule: Always use Seattle timezone for date-based operations**

All date calculations MUST use `ZoneId.of("America/Los_Angeles")` to ensure consistency across modules.

## **Why Seattle Timezone:**
- **ETL Module**: Parquet files partitioned by Seattle date (`s3://bucket/acsubmissions/2025/07/20/`)
- **Stats Module**: Must query same date partitions as ETL creates
- **Consistency**: Prevents timezone mismatches that cause "data not found" issues

## **Implementation Pattern:**
```java
// ✅ Correct
private static final ZoneId SEATTLE_ZONE = ZoneId.of("America/Los_Angeles");
LocalDate yesterday = LocalDate.now(SEATTLE_ZONE).minusDays(1);

// ❌ Wrong (uses system/UTC timezone)  
LocalDate yesterday = LocalDate.now().minusDays(1);
```

## **Where Applied:**
- ✅ `etl-stream-lambda/service/ParquetFileWriter.java` 
- ✅ `etl-stream-lambda/service/S3Service.java`
- ✅ `stats-lambda/handler/StatsHandler.java`
- ✅ `stats-lambda/service/AthenaQueryService.java`
- ✅ `stats-lambda/service/StatsCacheService.java`

## **Critical:**
**Never change timezone without updating ALL modules** - this will break data pipeline!