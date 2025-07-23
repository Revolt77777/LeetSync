package com.leetsync.infrastructure;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class LeetSyncRecommendationStack extends Stack {

    private final Function recommendationFunction;

    public LeetSyncRecommendationStack(final Construct scope, final String id, 
                                     final Table userStatsCacheTable, final Table recommendationsCacheTable) {
        this(scope, id, null, userStatsCacheTable, recommendationsCacheTable);
    }

    public LeetSyncRecommendationStack(final Construct scope, final String id, 
                                     final StackProps props, final Table userStatsCacheTable, final Table recommendationsCacheTable) {
        super(scope, id, props);

        // Recommendation Lambda Function
        this.recommendationFunction = Function.Builder.create(this, "RecommendationFunction")
                .functionName("leetsync-recommendation-lambda")
                .runtime(Runtime.JAVA_21)
                .code(Code.fromAsset("../recommendation-lambda/target/recommendation-lambda-1.0.0.jar"))
                .handler("com.leetsync.recommendation.handler.RecommendationHandler::handleRequest")
                .memorySize(512)
                .timeout(Duration.minutes(10))
                .environment(Map.of(
                        "STATS_CACHE_TABLE_NAME", userStatsCacheTable.getTableName(),
                        "RECOMMENDATIONS_CACHE_TABLE_NAME", recommendationsCacheTable.getTableName(),
                        "BEDROCK_REGION", "us-west-2",
                        "BEDROCK_MODEL_ID", "anthropic.claude-3-5-sonnet-20241022-v2:0"
                ))
                .build();

        // Grant DynamoDB permissions
        userStatsCacheTable.grantReadData(recommendationFunction);
        recommendationsCacheTable.grantReadWriteData(recommendationFunction);

        // Grant Bedrock permissions
        recommendationFunction.addToRolePolicy(PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .actions(List.of("bedrock:InvokeModel"))
                .resources(List.of("arn:aws:bedrock:us-west-2::foundation-model/anthropic.claude-3-5-sonnet-20241022-v2:0"))
                .build());

        // CloudWatch Events Rule for daily execution (6 AM PT = 1 PM UTC)
        Rule dailyRule = Rule.Builder.create(this, "DailyRecommendationRule")
                .schedule(Schedule.cron(
                        software.amazon.awscdk.services.events.CronOptions.builder()
                                .minute("0")
                                .hour("15")  // 8 AM Seattle = 14:00 UTC (standard time)
                                .build()))
                .description("Trigger recommendation lambda daily at 8 AM Seattle time")
                .build();

        dailyRule.addTarget(new LambdaFunction(recommendationFunction));
    }

    public Function getRecommendationFunction() {
        return recommendationFunction;
    }
}