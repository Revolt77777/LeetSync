package com.revolt7.leetsync.handler;

import com.revolt7.leetsync.model.AcSubmission;
import com.revolt7.leetsync.service.DynamoService;
import com.revolt7.leetsync.service.LeetCodeService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

@Component
public class SyncHandler {

    private final LeetCodeService leetCodeService;
    private final DynamoService dynamoService;

    public SyncHandler(LeetCodeService leetCodeService, DynamoService dynamoService) {
        this.leetCodeService = leetCodeService;
        this.dynamoService = dynamoService;
    }

    @Bean
    public Function<String, String> syncLeetCode() {
        return input -> {
            String username = "zxuanxu";
            int limit = 15;
            List<AcSubmission> submissions;

            try {
                submissions = leetCodeService.fetchRecentAcceptedSubmissions(username, limit);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            int newCount = 0;

            for (AcSubmission sub : submissions) {
                boolean stored = dynamoService.storeIfNew(sub);
                if (stored) {
                    newCount++;
                }
            }
            return "Synced submissions: " + newCount;
        };
    }
}
