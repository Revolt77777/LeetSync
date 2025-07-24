package com.leetsync.infrastructure;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class LeetSyncIngestionStack extends Stack {

    public LeetSyncIngestionStack(final Construct scope, final String id, final String resourceSuffix, final Table acSubmissionsTable, final Table usersTable) {
        this(scope, id, resourceSuffix, null, acSubmissionsTable, usersTable);
    }

    public LeetSyncIngestionStack(final Construct scope, final String id, final String resourceSuffix, final StackProps props, final Table acSubmissionsTable, final Table usersTable) {
        super(scope, id, props);

        // Ingestion Lambda Function
        Function ingestionFn = Function.Builder.create(this, "IngestionFunction")
                .functionName("leetsync-ingestion-lambda" + resourceSuffix)
                .runtime(Runtime.JAVA_21)
                .handler("com.leetsync.ingestion.handler.SyncHandler")
                .memorySize(256)
                .timeout(Duration.minutes(5))
                .code(Code.fromAsset("../ingestion-lambda/target/ingestion-lambda-1.0.0.jar"))
                .environment(Map.of(
                        "ACSUBMISSIONS_TABLE_NAME", acSubmissionsTable.getTableName(),
                        "USERS_TABLE_NAME", usersTable.getTableName(),
                        "LOG_LEVEL", "INFO",
                        "POLL_INTERVAL_MIN", "60"))
                .build();

        // Grant permissions to DynamoDB tables
        acSubmissionsTable.grantWriteData(ingestionFn);
        usersTable.grantReadWriteData(ingestionFn);


        // EventBridge Rule - Daily at 6 AM Seattle time (UTC-8/UTC-7)
        Rule dailyRule = Rule.Builder.create(this, "DailyIngestionRule")
                .ruleName("leetsync-daily-ingestion-rule" + resourceSuffix)
                .schedule(Schedule.cron(
                        software.amazon.awscdk.services.events.CronOptions.builder()
                                .minute("0")
                                .hour("14")  // 7 AM Seattle = 14:00 UTC (standard time)
                                .build()))
                .description("Trigger ingestion lambda daily at 7 AM Seattle time")
                .build();

        // Add Lambda as target
        dailyRule.addTarget(new LambdaFunction(ingestionFn));
    }
}