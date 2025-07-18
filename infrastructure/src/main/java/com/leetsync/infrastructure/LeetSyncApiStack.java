package com.leetsync.infrastructure;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class LeetSyncApiStack extends Stack {

    public LeetSyncApiStack(final Construct scope, final String id, final Table acSubmissionsTable, final Table usersTable) {
        super(scope, id);

        /* 2 ▶ Lambda packaging: Spring Boot JAR asset */
        Function apiFn = Function.Builder.create(this, "RestApiFunction")
                .runtime(Runtime.JAVA_21)
                .handler("com.leetsync.api.StreamLambdaHandler")
                .memorySize(1024)
                .timeout(Duration.seconds(15))
                .code(Code.fromAsset("../rest-api-server/target/leetsync-api-1.0.0.jar"))
                .environment(Map.of(
                        "ACSUBMISSIONS_TABLE_NAME", acSubmissionsTable.getTableName(),
                        "USERS_TABLE_NAME", usersTable.getTableName()))
                .build();

        /* least-privilege access */
        acSubmissionsTable.grantReadWriteData(apiFn);
        usersTable.grantReadWriteData(apiFn);

        /* 3 ▶ HTTP API with Lambda integration */
        HttpApi api = HttpApi.Builder.create(this, "LeetSyncHttpApi").build();

        HttpLambdaIntegration integration = HttpLambdaIntegration.Builder.create("RestApiIntegration", apiFn)
                .payloadFormatVersion(software.amazon.awscdk.services.apigatewayv2.PayloadFormatVersion.VERSION_2_0)
                .build();

        api.addRoutes(AddRoutesOptions.builder()
                .path("/{proxy+}") // This captures all paths
                .methods(List.of(
                        HttpMethod.GET,
                        HttpMethod.POST,
                        HttpMethod.PUT,
                        HttpMethod.DELETE,
                        HttpMethod.PATCH,
                        HttpMethod.OPTIONS // For CORS
                ))
                .integration(integration)
                .build());

        // Also add root path support
        api.addRoutes(AddRoutesOptions.builder()
                .path("/")
                .methods(List.of(HttpMethod.GET))
                .integration(integration)
                .build());

        /* 4 ▶ Stack outputs */
        CfnOutput.Builder.create(this, "ApiEndpoint")
                .value(api.getApiEndpoint())
                .description("Base URL of the LeetSync REST API")
                .build();

        CfnOutput.Builder.create(this, "ApiUrl")
                .value(api.getApiEndpoint() + "/acsubmissions")
                .description("AC Submissions endpoint URL")
                .build();
    }
}
