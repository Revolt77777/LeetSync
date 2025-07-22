package com.leetsync.infrastructure;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps;
import software.amazon.awscdk.services.dynamodb.StreamViewType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.LifecycleRule;
import software.amazon.awscdk.services.s3.StorageClass;
import software.amazon.awscdk.services.s3.Transition;
import software.constructs.Construct;

import java.util.List;

public class LeetSyncDataStack extends Stack {

    private final Table acSubmissionsTable;
    private final Table problemsTable;
    private final Table usersTable;
    private final Table userStatsCacheTable;
    private final Bucket parquetBucket;
    private final Bucket athenaResultsBucket;

    public LeetSyncDataStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public LeetSyncDataStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // AcSubmissions Table - Primary data store with streams for ETL
        this.acSubmissionsTable = Table.Builder.create(this, "AcSubmissionsTable")
                .tableName("AcSubmissions")
                .partitionKey(Attribute.builder()
                        .name("username")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("timestamp")
                        .type(AttributeType.NUMBER)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .stream(StreamViewType.NEW_IMAGE)
                .build();

        // Add GSI for problem-based queries
        acSubmissionsTable.addGlobalSecondaryIndex(GlobalSecondaryIndexProps.builder()
                .indexName("titleSlug-timestamp-index")
                .partitionKey(Attribute.builder()
                        .name("titleSlug")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("timestamp")
                        .type(AttributeType.NUMBER)
                        .build())
                .build());

        // Problems Table - LeetCode problem catalog
        this.problemsTable = Table.Builder.create(this, "ProblemsTable")
                .tableName("Problems")
                .partitionKey(Attribute.builder()
                        .name("titleSlug")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .removalPolicy(software.amazon.awscdk.RemovalPolicy.DESTROY)
                .build();

        // Users Table - User management
        this.usersTable = Table.Builder.create(this, "UsersTable")
                .tableName("Users")
                .partitionKey(Attribute.builder()
                        .name("username")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .removalPolicy(software.amazon.awscdk.RemovalPolicy.DESTROY)
                .build();

        // UserStatsCache Table - Calculated user statistics with TTL
        this.userStatsCacheTable = Table.Builder.create(this, "UserStatsCacheTable")
                .tableName("UserStatsCache")
                .partitionKey(Attribute.builder()
                        .name("username")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("statType")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .timeToLiveAttribute("ttl")
                .removalPolicy(software.amazon.awscdk.RemovalPolicy.DESTROY)
                .build();

        // S3 bucket for Parquet files
        this.parquetBucket = Bucket.Builder.create(this, "ParquetBucket")
                .bucketName("leetsync-parquet")
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .lifecycleRules(List.of(
                    LifecycleRule.builder()
                        .id("GlacierTransition")
                        .enabled(true)
                        .transitions(List.of(
                            Transition.builder()
                                .storageClass(StorageClass.GLACIER)
                                .transitionAfter(Duration.days(90))
                                .build()
                        ))
                        .build()
                ))
                .removalPolicy(software.amazon.awscdk.RemovalPolicy.DESTROY)
                .build();

        // S3 bucket for Athena query results
        this.athenaResultsBucket = Bucket.Builder.create(this, "AthenaResultsBucket")
                .bucketName("leetsync-athena-results")
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .lifecycleRules(List.of(
                    LifecycleRule.builder()
                        .id("DeleteOldResults")
                        .enabled(true)
                        .expiration(Duration.days(7))
                        .build()
                ))
                .removalPolicy(software.amazon.awscdk.RemovalPolicy.DESTROY)
                .build();
    }

    public Table getAcSubmissionsTable() {
        return acSubmissionsTable;
    }
    
    public Table getProblemsTable() {
        return problemsTable;
    }
    
    public Table getUsersTable() {
        return usersTable;
    }
    
    public Table getUserStatsCacheTable() {
        return userStatsCacheTable;
    }
    
    public Bucket getParquetBucket() {
        return parquetBucket;
    }
    
    public Bucket getAthenaResultsBucket() {
        return athenaResultsBucket;
    }
}