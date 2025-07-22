package com.leetsync.stats.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Athena service for querying parquet data via Glue Crawler discovered tables
 */
public class AthenaQueryService {
    
    private static final Logger logger = LoggerFactory.getLogger(AthenaQueryService.class);
    private static final String DATABASE = "leetsync";
    private static final String WORKGROUP = "leetsync-stats";
    private static final int MAX_WAIT_TIME_SECONDS = 300;
    private static final ZoneId SEATTLE_ZONE = ZoneId.of("America/Los_Angeles");
    
    private final AthenaClient athenaClient;
    
    public AthenaQueryService() {
        this.athenaClient = AthenaClient.builder()
                .region(Region.US_WEST_2)
                // Add connection pool configuration for Lambda
                .overrideConfiguration(builder -> builder
                    .retryPolicy(retryBuilder -> retryBuilder.numRetries(3)))
                .build();
    }
    
    public AthenaQueryService(AthenaClient athenaClient) {
        this.athenaClient = athenaClient;
    }
    
    public void ensureTableReady(String parquetBucketName) {
        try {
            String describeQuery = "DESCRIBE acsubmissions";
            executeQueryAndGetResults(describeQuery);
        } catch (Exception e) {
            logger.error("Table acsubmissions not accessible: {}", e.getMessage());
            throw new RuntimeException("Table not ready. Run Glue Crawler first.", e);
        }
    }
    
    /**
     * Gets yesterday's submission data for stats calculation
     */
    public ResultSet getYesterdayDailyStats(String username, LocalDate yesterday) {
        String[] dateParts = yesterday.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")).split("/");
        
        String query = String.format("""
            WITH daily_submissions AS (
                SELECT 
                    titleSlug,
                    difficultyLevel,
                    tags,
                    AVG(runtimeMs) as avgRuntimeMs,
                    AVG(memoryMb) as avgMemoryMb
                FROM acsubmissions 
                WHERE username = '%s' 
                AND year = '%s' 
                AND month = '%s' 
                AND day = '%s'
                GROUP BY titleSlug, difficultyLevel, tags
            ),
            exploded_tags AS (
                SELECT 
                    titleSlug,
                    difficultyLevel,
                    tag,
                    avgRuntimeMs,
                    avgMemoryMb
                FROM daily_submissions
                CROSS JOIN UNNEST(split(tags, ',')) AS t(tag)
                WHERE tags IS NOT NULL AND tags != ''
            )
            SELECT 
                'summary' as metric_type,
                'solved_count' as metric_key,
                COUNT(DISTINCT titleSlug) as count,
                0.0 as total_runtime,
                0.0 as total_memory,
                0.0 as avg_difficulty
            FROM daily_submissions
            
            UNION ALL
            
            SELECT 
                'difficulty' as metric_type,
                CAST(difficultyLevel AS VARCHAR) as metric_key,
                COUNT(DISTINCT titleSlug) as count,
                0.0 as total_runtime,
                0.0 as total_memory,
                0.0 as avg_difficulty
            FROM daily_submissions
            GROUP BY difficultyLevel
            
            UNION ALL
            
            SELECT 
                'tag' as metric_type,
                tag as metric_key,
                COUNT(DISTINCT titleSlug) as count,
                SUM(avgRuntimeMs) as total_runtime,
                SUM(avgMemoryMb) as total_memory,
                AVG(CAST(difficultyLevel AS DOUBLE)) as avg_difficulty
            FROM exploded_tags
            GROUP BY tag
            """, username, dateParts[0], dateParts[1], dateParts[2]);
            
        return executeQueryAndGetResults(query);
    }
    
    
    
    /**
     * Gets users who had submissions yesterday
     */
    public ResultSet getYesterdayActiveUsers(LocalDate yesterday) {
        String[] dateParts = yesterday.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")).split("/");
        
        String query = String.format("""
            SELECT DISTINCT username 
            FROM acsubmissions 
            WHERE year = '%s' 
            AND month = '%s' 
            AND day = '%s'
            """, dateParts[0], dateParts[1], dateParts[2]);
            
        return executeQueryAndGetResults(query);
    }
    
    
    private String executeQuery(String query) {
        try {
            StartQueryExecutionRequest startRequest = StartQueryExecutionRequest.builder()
                    .queryString(query)
                    .queryExecutionContext(QueryExecutionContext.builder()
                            .database(DATABASE)
                            .build())
                    .workGroup(WORKGROUP)
                    .build();
                    
            StartQueryExecutionResponse startResponse = athenaClient.startQueryExecution(startRequest);
            String queryExecutionId = startResponse.queryExecutionId();
            
            // Wait for query to complete
            waitForQueryCompletion(queryExecutionId);
            
            return queryExecutionId;
            
        } catch (Exception e) {
            logger.error("Error executing query: {}", query, e);
            throw new RuntimeException("Failed to execute Athena query", e);
        }
    }
    
    private ResultSet executeQueryAndGetResults(String query) {
        String queryExecutionId = executeQuery(query);
        return getQueryResults(queryExecutionId);
    }
    
    private void waitForQueryCompletion(String queryExecutionId) {
        int waitTime = 0;
        
        while (waitTime < MAX_WAIT_TIME_SECONDS) {
            GetQueryExecutionRequest getRequest = GetQueryExecutionRequest.builder()
                    .queryExecutionId(queryExecutionId)
                    .build();
                    
            GetQueryExecutionResponse getResponse = athenaClient.getQueryExecution(getRequest);
            QueryExecutionStatus status = getResponse.queryExecution().status();
            
            if (status.state() == QueryExecutionState.SUCCEEDED) {
                return;
            } else if (status.state() == QueryExecutionState.FAILED || 
                      status.state() == QueryExecutionState.CANCELLED) {
                String error = status.stateChangeReason();
                logger.error("Query {} failed: {}", queryExecutionId, error);
                throw new RuntimeException("Query failed: " + error);
            }
            
            try {
                TimeUnit.SECONDS.sleep(2);
                waitTime += 2;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Query execution interrupted", e);
            }
        }
        
        throw new RuntimeException("Query execution timed out after " + MAX_WAIT_TIME_SECONDS + " seconds");
    }
    
    private ResultSet getQueryResults(String queryExecutionId) {
        try {
            GetQueryResultsRequest getResultsRequest = GetQueryResultsRequest.builder()
                    .queryExecutionId(queryExecutionId)
                    .build();
                    
            GetQueryResultsResponse getResultsResponse = athenaClient.getQueryResults(getResultsRequest);
            
                    
            return getResultsResponse.resultSet();
            
        } catch (Exception e) {
            logger.error("Error getting query results for execution ID: {}", queryExecutionId, e);
            throw new RuntimeException("Failed to get query results", e);
        }
    }
    
    public void close() {
        if (athenaClient != null) {
            athenaClient.close();
        }
    }
}