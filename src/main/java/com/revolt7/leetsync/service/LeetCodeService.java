package com.revolt7.leetsync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolt7.leetsync.model.AcSubmission;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
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
                           id title timestamp } }",
              "variables": {
                "username": "%s",
                "limit": %d
              }
            }
            """, username, limit);
    }

    String getHttpResponse(String queryJson) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://leetcode.com/graphql/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(queryJson))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> res =
                client.send(req, HttpResponse.BodyHandlers.ofString());
        return res.body();
    }

    List<AcSubmission> parseResponse(String response) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Read the full JSON into a tree
            var root = mapper.readTree(response);

            // Navigate to the array node
            var listNode = root.path("data").path("recentAcSubmissionList");

            // Convert directly to List<AcSubmission>
            return mapper.readerForListOf(AcSubmission.class).readValue(listNode);
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
