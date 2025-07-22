package com.leetsync.infrastructure;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.athena.CfnWorkGroup;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.glue.CfnCrawler;
import software.amazon.awscdk.services.glue.CfnDatabase;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class LeetSyncAnalyticsStack extends Stack {

    private final CfnWorkGroup athenaWorkGroup;
    private final Table userStatsCacheTable;
    private final Function statsFunction;
    private final Rule dailyStatsRule;
    private final CfnDatabase glueDatabase;
    private final CfnCrawler glueCrawler;

    public LeetSyncAnalyticsStack(final Construct scope, final String id, 
                                 final Bucket parquetBucket, final Bucket athenaResultsBucket, 
                                 final Table userStatsCacheTable) {
        this(scope, id, null, parquetBucket, athenaResultsBucket, userStatsCacheTable);
    }

    public LeetSyncAnalyticsStack(final Construct scope, final String id, final StackProps props,
                                 final Bucket parquetBucket, final Bucket athenaResultsBucket,
                                 final Table userStatsCacheTable) {
        super(scope, id, props);

        this.userStatsCacheTable = userStatsCacheTable;

        // Create Glue Database
        this.glueDatabase = CfnDatabase.Builder.create(this, "LeetSyncGlueDatabase")
                .catalogId(this.getAccount())
                .databaseInput(CfnDatabase.DatabaseInputProperty.builder()
                        .name("leetsync")
                        .description("Database for LeetSync parquet data")
                        .build())
                .build();

        // Create IAM Role for Glue Crawler
        Role crawlerRole = Role.Builder.create(this, "GlueCrawlerRole")
                .assumedBy(new ServicePrincipal("glue.amazonaws.com"))
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSGlueServiceRole")
                ))
                .build();

        // Grant S3 access to crawler
        parquetBucket.grantRead(crawlerRole);

        // Create Glue Crawler with schedule (10 minutes before stats Lambda)
        this.glueCrawler = CfnCrawler.Builder.create(this, "LeetSyncParquetCrawler")
                .name("leetsync-parquet-crawler")
                .role(crawlerRole.getRoleArn())
                .databaseName(glueDatabase.getRef())
                .targets(CfnCrawler.TargetsProperty.builder()
                        .s3Targets(List.of(CfnCrawler.S3TargetProperty.builder()
                                .path("s3://" + parquetBucket.getBucketName() + "/acsubmissions/")
                                .build()))
                        .build())
                .description("Crawler for LeetSync parquet files - runs 10min before stats Lambda")
                .schedule(CfnCrawler.ScheduleProperty.builder()
                        .scheduleExpression("cron(50 14 * * ? *)")  // Daily at 14:50 UTC (6:50 AM Seattle, 10min before stats)
                        .build())
                .schemaChangePolicy(CfnCrawler.SchemaChangePolicyProperty.builder()
                        .updateBehavior("UPDATE_IN_DATABASE")
                        .deleteBehavior("DELETE_FROM_DATABASE")
                        .build())
                .build();

        // Create Athena WorkGroup for stats queries
        this.athenaWorkGroup = CfnWorkGroup.Builder.create(this, "LeetSyncWorkGroup")
                .name("leetsync-stats")
                .description("WorkGroup for LeetSync stats analytics queries")
                .workGroupConfiguration(CfnWorkGroup.WorkGroupConfigurationProperty.builder()
                        .resultConfiguration(CfnWorkGroup.ResultConfigurationProperty.builder()
                                .outputLocation("s3://" + athenaResultsBucket.getBucketName() + "/")
                                .build())
                        .enforceWorkGroupConfiguration(true)
                        .build())
                .build();

        // Stats Lambda Function
        this.statsFunction = Function.Builder.create(this, "StatsFunction")
                .functionName("leetsync-stats-calculator")
                .runtime(Runtime.JAVA_21)
                .handler("com.leetsync.stats.handler.StatsHandler::handleRequest")
                .memorySize(1024)
                .timeout(Duration.minutes(15))
                .code(Code.fromAsset("../stats-lambda/target/stats-lambda-1.0.0-shaded.jar"))
                .environment(Map.of(
                    "PARQUET_BUCKET_NAME", parquetBucket.getBucketName(),
                    "ATHENA_RESULTS_BUCKET_NAME", athenaResultsBucket.getBucketName(),
                    "STATS_CACHE_TABLE_NAME", userStatsCacheTable.getTableName()
                ))
                .build();

        // Grant permissions to Lambda
        userStatsCacheTable.grantReadWriteData(statsFunction);
        parquetBucket.grantRead(statsFunction);
        athenaResultsBucket.grantReadWrite(statsFunction);
        
        // Additional permissions for Athena and Glue
        statsFunction.addToRolePolicy(PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .actions(List.of(
                    "athena:StartQueryExecution",
                    "athena:GetQueryExecution",
                    "athena:GetQueryResults",
                    "athena:StopQueryExecution",
                    "athena:GetWorkGroup"
                ))
                .resources(List.of("*"))
                .build());

        statsFunction.addToRolePolicy(PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .actions(List.of(
                    "glue:GetDatabase",
                    "glue:GetTable",
                    "glue:GetTables",
                    "glue:CreateTable",
                    "glue:CreateDatabase",
                    "glue:DeleteTable",
                    "glue:GetPartitions"
                ))
                .resources(List.of("*"))
                .build());

        // EventBridge Rule - Daily at 7 AM Seattle time (15:00 UTC, 1 hour after ETL)
        this.dailyStatsRule = Rule.Builder.create(this, "DailyStatsRule")
                .schedule(Schedule.cron(
                        software.amazon.awscdk.services.events.CronOptions.builder()
                                .minute("0")
                                .hour("15")  // 7 AM Seattle = 15:00 UTC (1 hour after ETL/ingestion)
                                .build()))
                .description("Trigger stats calculation daily at 7 AM Seattle time (1 hour after ETL)")
                .build();

        // Add Lambda as target
        dailyStatsRule.addTarget(new LambdaFunction(statsFunction));
    }

    public CfnWorkGroup getAthenaWorkGroup() {
        return athenaWorkGroup;
    }

    public Table getUserStatsCacheTable() {
        return userStatsCacheTable;
    }

    public Function getStatsFunction() {
        return statsFunction;
    }

    public Rule getDailyStatsRule() {
        return dailyStatsRule;
    }
}