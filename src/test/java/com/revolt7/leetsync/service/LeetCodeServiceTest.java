package com.revolt7.leetsync.service;

import com.revolt7.leetsync.model.AcSubmission;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeetCodeServiceTest {

    private final LeetCodeService service = new LeetCodeService();

    @Test
    void buildQueryJson_shouldContainUsernameAndLimit() {
        String json = service.buildQueryJson("testuser", 3);
        assertTrue(json.contains("\"username\": \"testuser\""));
        assertTrue(json.contains("\"limit\": 3"));
    }

    @Test
    void parseResponse_shouldReturnSubmissions() {
        String mockJson = """
            {
              "data": {
                "recentAcSubmissionList": [
                  {
                    "id": "1688968015",
                    "title": "Two Sum",
                    "timestamp": 1720000000
                  },
                  {
                    "id": "1688781454",
                    "title": "Add Two Numbers",
                    "timestamp": 1720000500
                  }
                ]
              }
            }
            """;

        List<AcSubmission> result = service.parseResponse(mockJson);
        assertEquals(2, result.size());
        assertEquals(1688781454L, result.get(1).getId());
        assertEquals("Two Sum", result.get(0).getTitle());
        assertEquals(1720000000L, result.get(0).getTimestamp());
    }
}
