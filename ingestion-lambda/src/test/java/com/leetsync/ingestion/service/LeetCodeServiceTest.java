package com.leetsync.ingestion.service;

import com.leetsync.ingestion.model.AcSubmission;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                    "title": "Two Sum",
                    "titleSlug": "two-sum",
                    "timestamp": 1720000000,
                    "runtime": "0 ms",
                    "memory": "19.1 MB"
                  },
                  {
                    "title": "Add Two Numbers",
                    "titleSlug": "add-two-numbers",
                    "timestamp": 1720000500,
                    "runtime": "12 ms",
                    "memory": "25.5 MB"
                  }
                ]
              }
            }
            """;

        List<AcSubmission> result = service.parseResponse(mockJson);
        assertEquals(2, result.size());
        assertEquals("Two Sum", result.get(0).getTitle());
        assertEquals("two-sum", result.get(0).getTitleSlug());
        assertEquals(1720000000L, result.get(0).getTimestamp());
        assertEquals(0, result.get(0).getRuntimeMs());
        assertEquals(19.1, result.get(0).getMemoryMb());
        assertEquals("Add Two Numbers", result.get(1).getTitle());
        assertEquals("add-two-numbers", result.get(1).getTitleSlug());
        assertEquals(1720000500L, result.get(1).getTimestamp());
        assertEquals(12, result.get(1).getRuntimeMs());
        assertEquals(25.5, result.get(1).getMemoryMb());
    }

    @Test
    void parseResponse_shouldHandleNullRuntimeMemory() {
        String mockJson = """
            {
              "data": {
                "recentAcSubmissionList": [
                  {
                    "title": "Two Sum",
                    "titleSlug": "two-sum",
                    "timestamp": 1720000000,
                    "runtime": null,
                    "memory": null
                  }
                ]
              }
            }
            """;

        List<AcSubmission> result = service.parseResponse(mockJson);
        assertEquals(1, result.size());
        assertEquals("Two Sum", result.get(0).getTitle());
        assertEquals(null, result.get(0).getRuntimeMs());
        assertEquals(null, result.get(0).getMemoryMb());
    }
}
