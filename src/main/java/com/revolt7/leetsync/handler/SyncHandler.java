package com.revolt7.leetsync.handler;

import com.revolt7.leetsync.model.AcSubmission;
import com.revolt7.leetsync.service.DynamoService;
import com.revolt7.leetsync.service.LeetCodeService;
import com.revolt7.leetsync.service.NotionService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

@Component
public class SyncHandler {

    private final LeetCodeService leetCodeService;
    private final DynamoService dynamoService;
    private final NotionService notionService;

    public SyncHandler(LeetCodeService leetCodeService, DynamoService dynamoService, NotionService notionService) {
        this.leetCodeService = leetCodeService;
        this.dynamoService = dynamoService;
        this.notionService = notionService;
    }

    @Bean
    public Function<String, String> syncLeetCode() {
        return input -> {
            String username = System.getenv("LEETCODE_USERNAME");
            int limit = 15;

            try {
                List<AcSubmission> submissions = leetCodeService.fetchRecentAcceptedSubmissions(username, limit);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            int newCount = 0;

            /*for (AcSubmission sub : submissions) {
                boolean stored = dynamoService.storeIfNew(sub);
                if (stored) {
                    notionService.sendToNotion(sub);
                    newCount++;
                }
            }*/
            return "Synced submissions: " + newCount;
        };
    }
}
