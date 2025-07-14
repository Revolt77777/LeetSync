package com.leetsync.ingestion.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.leetsync.shared.model.AcSubmission;
import com.leetsync.ingestion.service.LeetCodeService;
import com.leetsync.ingestion.service.DynamoService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.util.List;

public class SyncHandler implements RequestHandler<Void, String> {

    private final LeetCodeService leetCodeService;
    private final DynamoService dynamoService;

    public SyncHandler() {
        // Manual dependency wiring (simple and fast for Lambda)
        this.leetCodeService = new LeetCodeService();
        this.dynamoService = new DynamoService(DynamoDbClient.create(), System.getenv("DYNAMODB_TABLE_NAME"));
    }

    @Override
    public String handleRequest(Void input, Context context) {
        String username = "zxuanxu";
        int limit = 15;
        List<AcSubmission> submissions;

        try {
            submissions = leetCodeService.fetchRecentAcceptedSubmissions(username, limit);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch submissions", e);
        }

        int newCount = 0;
        for (AcSubmission sub : submissions) {
            boolean stored = dynamoService.storeIfNew(sub);
            if (stored) {
                newCount++;
            }
        }

        return "Synced submissions: " + newCount;
    }
}
