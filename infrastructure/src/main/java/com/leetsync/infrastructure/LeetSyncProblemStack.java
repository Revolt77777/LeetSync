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

import java.util.Map;

public class LeetSyncProblemStack extends Stack {

    public LeetSyncProblemStack(final Construct scope, final String id, final String resourceSuffix, final Table problemsTable) {
        this(scope, id, resourceSuffix, null, problemsTable);
    }

    public LeetSyncProblemStack(final Construct scope, final String id, final String resourceSuffix, final StackProps props, final Table problemsTable) {
        super(scope, id, props);

        // Problem Scan Lambda Function
        Function problemScanFn = Function.Builder.create(this, "ProblemScanFunction")
                .functionName("leetsync-problem-lambda" + resourceSuffix)
                .runtime(Runtime.JAVA_21)
                .handler("com.leetsync.problem.handler.ProblemScanHandler")
                .memorySize(512)
                .timeout(Duration.minutes(15))
                .code(Code.fromAsset("../problem-lambda/target/problem-lambda-1.0.0.jar"))
                .environment(Map.of(
                        "PROBLEMS_TABLE_NAME", problemsTable.getTableName(),
                        "LOG_LEVEL", "INFO",
                        "LEETCODE_GRAPHQL_URL", "https://leetcode.com/graphql"))
                .build();

        // Grant read/write permissions to Problems DynamoDB table
        problemsTable.grantReadWriteData(problemScanFn);

        // EventBridge Rule - Weekly at 5 AM Seattle time every Monday
        Rule weeklyRule = Rule.Builder.create(this, "WeeklyProblemScanRule")
                .ruleName("leetsync-weekly-problem-scan-rule" + resourceSuffix)
                .schedule(Schedule.cron(
                        software.amazon.awscdk.services.events.CronOptions.builder()
                                .minute("0")
                                .hour("13")  // 5 AM Seattle = 13:00 UTC (standard time)
                                .weekDay("MON")
                                .build()))
                .description("Trigger problem scan lambda weekly at 5 AM Seattle time on Monday")
                .build();

        // Add Lambda as target
        weeklyRule.addTarget(new LambdaFunction(problemScanFn));
    }
}