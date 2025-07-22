# 📊 LeetSync Stats Module - Complete Implementation Guide

## 🎯 Module Overview

Build a stats analytics system that processes daily Parquet files from S3, calculates user performance metrics using Athena, and serves them via REST API with DynamoDB caching.

### Architecture Flow:

`Problem lambda finished storing S3 Parquet → Stats Lambda → Athena Queries → DynamoDB Cache → Spring Boot API → JSON Response`

---

### A. Data Model & Storage Design

**Athena Table Schema:**

`acsubmissions (
  username, title, titleSlug, timestamp, 
  runtimeMs, memoryMb, difficultyLevel, 
  tags[], acRate, totalAccepted, totalSubmitted
)
PARTITIONED BY (rollup_date) -- Using year/month/day format`

**DynamoDB Cache Structure:**

`Table: UserStatsCache
   Partition Key: username (STRING)
   Sort Key: statType (STRING)

1. Daily Snapshots
   PK: username, SK: DAILY#2024-07-19
   data: { solvedCount, byDifficulty, tagStats, problemsSolved[] }
   ttl: 30 days

2. 7-Day Aggregate  
   PK: username, SK: STATS_7DAY
   data: { complete 7-day calculations }
   ttl: 25 hours (expires after next daily run)

3. Lifetime Totals
   PK: username, SK: TOTAL_STATS  
   data: { running totals, never expires }

Note: To get latest daily stats, query statType beginning with "DAILY#" sorted descending, or calculate current date and query DAILY#{today}`

### B. Exact Metrics to Calculate

**Complete Stats API Response Structure:**

1. **Summary**
   - `yesterdaySolvedCount`: COUNT(DISTINCT titleSlug) WHERE date = yesterday
   - `totalSolvedCount`: Running total in DynamoDB, increment daily
   - `7DayAverage`: SUM(last 7 daily counts) / 7
   - `currentStreak`: Count consecutive days with submissions (needs special logic)
   - `longestStreak`: Historical max (stored in DynamoDB)

2. **Difficulty**
   - **yesterday**
     - easy: { count, percentage }
     - medium: { count, percentage }
     - hard: { count, percentage }
   - **total**
     - easy: { count, percentage }
     - medium: { count, percentage }
     - hard: { count, percentage }

3. **Tags**
   - **7-day**
     - tag1: { count, averageRuntimeMs, averageMemoryMb, averageDifficultyLevel }
     - tag2: { count, averageRuntimeMs, averageMemoryMb, averageDifficultyLevel }
     - ...
   - **total**
     - tag1: { count, averageRuntimeMs, averageMemoryMb, averageDifficultyLevel }
     - tag2: { count, averageRuntimeMs, averageMemoryMb, averageDifficultyLevel }
     - ...

**2. Difficulty Metrics:**

```sql
-- For each time period (yesterday/total):
SELECT 
    COUNT(DISTINCT CASE WHEN difficultyLevel = 1 THEN titleSlug END) as easy,
    COUNT(DISTINCT CASE WHEN difficultyLevel = 2 THEN titleSlug END) as medium,
    COUNT(DISTINCT CASE WHEN difficultyLevel = 3 THEN titleSlug END) as hard
FROM acsubmissions 
WHERE username = ? AND rollup_date = ?
```

**3. Tag Metrics:**

```sql
-- Explode tags array and calculate:
WITH exploded AS (
    SELECT username, titleSlug, runtimeMs, memoryMb, difficultyLevel, tag
    FROM acsubmissions 
    CROSS JOIN UNNEST(tags) AS t(tag)
    WHERE username = ? AND rollup_date BETWEEN ? AND ?
)
SELECT 
    tag, 
    COUNT(DISTINCT titleSlug) as count,
    AVG(runtimeMs) as averageRuntimeMs,
    AVG(memoryMb) as averageMemoryMb,
    AVG(difficultyLevel) as averageDifficultyLevel
FROM exploded 
GROUP BY tag
```

### C. Daily Calculation Process

**Step 1: Extract Yesterday's Data**

`Query: SELECT * FROM acsubmissions WHERE username = ? AND rollup_date = yesterday
Result: List of yesterday's submissions`

**Step 2: Calculate Daily Metrics**

- Group by titleSlug to handle multiple submissions of same problem
- Count distinct problems by difficulty
- Calculate tag statistics

**Step 3: Update Running Totals**

```
1. Fetch userId#TOTAL_STATS from DynamoDB
2. For each new distinct problem solved yesterday:
   - Increment totalSolvedCount
   - Update difficulty counts
   - Update tag totals (weighted average for runtime/memory)
3. Save back to DynamoDB
```

**Step 4: Calculate 7-Day Stats**

```
Rolling Window Method:
1. Fetch daily snapshots for last 8 days
2. Remove day 8 metrics, add yesterday's metrics
3. Recalculate averages and totals
4. Save as userId#STATS_7DAY

For distinct problem counts:
- Query Athena for last 7 partitions
- COUNT(DISTINCT titleSlug) GROUP BY difficulty, tag
```

**Step 5: Calculate Streaks**

```
1. Fetch last 30 daily snapshots
2. Starting from yesterday, count backwards while solvedCount > 0
3. Update currentStreak
4. If currentStreak > longestStreak, update longestStreak
```

### D. Design Decisions & Clarifications

**Project Context:** SDE new-grad/internship portfolio project - optimizing for architectural demonstration over enterprise scale.

**Key Concerns & Resolutions:**

1. **Data Consistency**: Stats lambda timing vs problem lambda completion
   - *Resolution*: Use EventBridge scheduling with sufficient delay after problem lambda expected completion

2. **Error Handling Strategy**: 
   - *Approach*: Basic try-catch with CloudWatch logging (appropriate for portfolio scope)
   - Retry logic for Athena query failures
   - Graceful degradation when data unavailable

3. **No Submissions Handling**:
   - *Resolution*: Store zero values, maintain streak-breaking logic

4. **Processing Scope**:
   - *Decision*: Process all users daily (simple approach for demo purposes)
   - Future optimization: Process only active users

5. **Cost Considerations**:
   - Athena queries batched where possible
   - DynamoDB designed for efficient access patterns
   - Acceptable cost for portfolio demonstration

**Implementation Priorities:**
1. DynamoDB UserStatsCache table structure
2. Stats Lambda handler framework  
3. Athena query integration
4. EventBridge scheduling setup