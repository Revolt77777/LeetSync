package com.leetsync.ingestion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leetsync.shared.model.AcSubmission;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class LeetCodeService {

    public List<AcSubmission> fetchRecentAcceptedSubmissions(String username, int limit) throws IOException, InterruptedException {
        String queryJson = buildQueryJson(username, limit);
        String res = getHttpResponse(queryJson);
        return parseResponse(res);
    }

    String buildQueryJson(String username, int limit) {
        return String.format("""
            {
              "query": "query recentAcSubmissions($username: String!, $limit: Int!) { \
                         recentAcSubmissionList(username: $username, limit: $limit) { \
                           title titleSlug timestamp runtime memory } }",
              "variables": {
                "username": "%s",
                "limit": %d
              }
            }
            """, username, limit);
    }

    private String getHttpResponse(String queryJson) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://leetcode.com/graphql/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(queryJson))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        return res.body();
    }

    List<AcSubmission> parseResponse(String response) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            var root = mapper.readTree(response);
            var listNode = root.path("data").path("recentAcSubmissionList");
            
            // Parse each submission manually to handle runtime/memory parsing
            List<AcSubmission> submissions = new java.util.ArrayList<>();
            
            for (var node : listNode) {
                AcSubmission submission = new AcSubmission();
                submission.setTitle(node.path("title").asText());
                submission.setTitleSlug(node.path("titleSlug").asText());
                submission.setTimestamp(node.path("timestamp").asLong());
                
                // Parse runtime: "0 ms" -> 0
                String runtimeStr = node.path("runtime").asText();
                submission.setRuntimeMs(parseRuntime(runtimeStr));
                
                // Parse memory: "19.1 MB" -> 19.1
                String memoryStr = node.path("memory").asText();
                submission.setMemoryMb(parseMemory(memoryStr));
                
                submissions.add(submission);
            }
            
            return submissions;
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    private Integer parseRuntime(String runtimeStr) {
        if (runtimeStr == null || runtimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            // Extract number from "0 ms" or similar format
            String numStr = runtimeStr.replaceAll("[^0-9]", "");
            return numStr.isEmpty() ? null : Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Double parseMemory(String memoryStr) {
        if (memoryStr == null || memoryStr.trim().isEmpty()) {
            return null;
        }
        try {
            // Extract number from "19.1 MB" or similar format
            String numStr = memoryStr.replaceAll("[^0-9.]", "");
            return numStr.isEmpty() ? null : Double.parseDouble(numStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
