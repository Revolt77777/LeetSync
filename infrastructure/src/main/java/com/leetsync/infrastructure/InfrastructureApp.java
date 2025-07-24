package com.leetsync.infrastructure;

import software.amazon.awscdk.App;

public class InfrastructureApp {
    public static void main(final String[] args) {
        App app = new App();

        // Get environment from context (test or production)
        String environment = (String) app.getNode().tryGetContext("environment");
        if (environment == null) {
            environment = "test"; // Default to test environment
        }

        // Create environment-specific stack names
        String envPrefix = environment.equals("production") ? "LeetSync" : "LeetSync-" + environment;

        // Create data stack first
        LeetSyncDataStack dataStack = new LeetSyncDataStack(app, envPrefix + "DataStack");
        
        // Pass table references to other stacks
        new LeetSyncApiStack(app, envPrefix + "ApiStack", dataStack.getAcSubmissionsTable(), dataStack.getUsersTable(), dataStack.getUserStatsCacheTable(), dataStack.getRecommendationsCacheTable());
        new LeetSyncIngestionStack(app, envPrefix + "IngestionStack", dataStack.getAcSubmissionsTable(), dataStack.getUsersTable());
        new LeetSyncProblemStack(app, envPrefix + "ProblemStack", dataStack.getProblemsTable());
        new LeetSyncEtlStack(app, envPrefix + "EtlStack", dataStack.getAcSubmissionsTable(), dataStack.getProblemsTable(), dataStack.getParquetBucket());
        new LeetSyncAnalyticsStack(app, envPrefix + "AnalyticsStack", dataStack.getParquetBucket(), dataStack.getAthenaResultsBucket(), dataStack.getUserStatsCacheTable());
        new LeetSyncRecommendationStack(app, envPrefix + "RecommendationStack", dataStack.getUserStatsCacheTable(), dataStack.getRecommendationsCacheTable());

        app.synth();
    }

}


