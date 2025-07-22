package com.leetsync.ingestion.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.leetsync.shared.model.AcSubmission;
import com.leetsync.ingestion.service.LeetCodeService;
import com.leetsync.ingestion.service.DynamoService;
import com.leetsync.ingestion.service.UserService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.util.List;

public class SyncHandler implements RequestHandler<Void, String> {

    private final LeetCodeService leetCodeService;
    private final DynamoService dynamoService;
    private final UserService userService;

    public SyncHandler() {
        // Manual dependency wiring (simple and fast for Lambda)
        DynamoDbClient dynamoClient = DynamoDbClient.create();
        this.leetCodeService = new LeetCodeService();
        this.dynamoService = new DynamoService(dynamoClient, System.getenv("ACSUBMISSIONS_TABLE_NAME"));
        this.userService = new UserService(dynamoClient, System.getenv("USERS_TABLE_NAME"));
    }

    @Override
    public String handleRequest(Void input, Context context) {
        // Get all users from Users table
        List<String> usernames = userService.getAllUsernames();
        context.getLogger().log("Found " + usernames.size() + " users to sync");
        
        int totalNewSubmissions = 0;
        int limit = 20;
        
        for (String username : usernames) {
            try {
                context.getLogger().log("Syncing user: " + username);
                
                // Fetch recent submissions for this user
                List<AcSubmission> submissions = leetCodeService.fetchRecentAcceptedSubmissions(username, limit);
                
                // Store new submissions
                int userNewCount = 0;
                for (AcSubmission sub : submissions) {
                    // Set the username in the submission
                    sub.setUsername(username);
                    
                    boolean stored = dynamoService.storeIfNew(sub);
                    if (stored) {
                        userNewCount++;
                    }
                }
                
                // Update user's lastSync timestamp
                if (userNewCount > 0) {
                    userService.updateLastSync(username, System.currentTimeMillis());
                }
                
                totalNewSubmissions += userNewCount;
                context.getLogger().log("User " + username + ": " + userNewCount + " new submissions");
                
            } catch (IOException | InterruptedException e) {
                context.getLogger().log("Failed to sync user " + username + ": " + e.getMessage());
                // Continue with next user instead of failing completely
            }
        }

        return "Synced " + usernames.size() + " users with " + totalNewSubmissions + " total new submissions";
    }

}
