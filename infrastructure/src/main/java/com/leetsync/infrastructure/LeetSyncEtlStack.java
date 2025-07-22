package com.leetsync.infrastructure;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.eventsources.DynamoEventSource;
import software.amazon.awscdk.services.lambda.StartingPosition;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class LeetSyncEtlStack extends Stack {

    private final Function etlFunction;

    public LeetSyncEtlStack(final Construct scope, final String id,
                            final Table acSubmissionsTable, final Table problemsTable, final Bucket parquetBucket) {
        this(scope, id, null, acSubmissionsTable, problemsTable, parquetBucket);
    }

    public LeetSyncEtlStack(final Construct scope, final String id, final StackProps props,
                            final Table acSubmissionsTable, final Table problemsTable, final Bucket parquetBucket) {
        super(scope, id, props);

        // Create ETL Lambda function
        this.etlFunction = Function.Builder.create(this, "EtlStreamFunction")
                .functionName("leetsync-etl-stream")
                .runtime(Runtime.JAVA_21)
                .code(Code.fromAsset("../etl-stream-lambda/target/etl-stream-lambda-1.0.0-shaded.jar"))
                .handler("com.leetsync.etl.handler.StreamHandler::handleRequest")
                .memorySize(512)
                .timeout(Duration.minutes(3))
                .environment(Map.of(
                    "DEST_BUCKET", parquetBucket.getBucketName(),
                    "PROBLEMS_TABLE_NAME", problemsTable.getTableName()
                ))
                .build();

        // Grant permissions to Lambda
        // DynamoDB permissions for Problems table
        etlFunction.addToRolePolicy(PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .actions(List.of("dynamodb:GetItem"))
                .resources(List.of(problemsTable.getTableArn()))
                .build());

        // DynamoDB Stream permissions for AcSubmissions table
        etlFunction.addToRolePolicy(PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .actions(List.of(
                    "dynamodb:DescribeStream",
                    "dynamodb:GetRecords",
                    "dynamodb:GetShardIterator",
                    "dynamodb:ListStreams"
                ))
                .resources(List.of(acSubmissionsTable.getTableStreamArn()))
                .build());

        // S3 permissions for Parquet bucket
        parquetBucket.grantWrite(etlFunction);


        // Add DynamoDB Stream event source
        etlFunction.addEventSource(DynamoEventSource.Builder.create(acSubmissionsTable)
                .startingPosition(StartingPosition.LATEST)
                .batchSize(100)
                .build());
    }

    public Function getEtlFunction() {
        return etlFunction;
    }

}