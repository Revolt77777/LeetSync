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

        // Create environment-specific stack names and resource suffixes
        String envPrefix = environment.equals("production") ? "LeetSync" : "LeetSync-" + environment;
        String resourceSuffix = environment.equals("production") ? "" : "-" + environment;

        // Create data stack first
        LeetSyncDataStack dataStack = new LeetSyncDataStack(app, envPrefix + "DataStack", resourceSuffix);
        
        // Pass table references to other stacks
        new LeetSyncApiStack(app, envPrefix + "ApiStack", resourceSuffix, dataStack.getAcSubmissionsTable(), dataStack.getUsersTable(), dataStack.getUserStatsCacheTable(), dataStack.getRecommendationsCacheTable());
        new LeetSyncIngestionStack(app, envPrefix + "IngestionStack", resourceSuffix, dataStack.getAcSubmissionsTable(), dataStack.getUsersTable());
        new LeetSyncProblemStack(app, envPrefix + "ProblemStack", resourceSuffix, dataStack.getProblemsTable());
        new LeetSyncEtlStack(app, envPrefix + "EtlStack", resourceSuffix, dataStack.getAcSubmissionsTable(), dataStack.getProblemsTable(), dataStack.getParquetBucket());
        new LeetSyncAnalyticsStack(app, envPrefix + "AnalyticsStack", resourceSuffix, dataStack.getParquetBucket(), dataStack.getAthenaResultsBucket(), dataStack.getUserStatsCacheTable());
        new LeetSyncRecommendationStack(app, envPrefix + "RecommendationStack", resourceSuffix, dataStack.getUserStatsCacheTable(), dataStack.getRecommendationsCacheTable());

        app.synth();
    }

}


