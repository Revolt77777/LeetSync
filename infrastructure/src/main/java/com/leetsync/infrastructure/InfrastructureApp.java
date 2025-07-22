package com.leetsync.infrastructure;

import software.amazon.awscdk.App;

public class InfrastructureApp {
    public static void main(final String[] args) {
        App app = new App();

        // Create data stack first
        LeetSyncDataStack dataStack = new LeetSyncDataStack(app, "LeetSyncDataStack");
        
        // Pass table references to other stacks
        new LeetSyncApiStack(app, "LeetSyncApiStack", dataStack.getAcSubmissionsTable(), dataStack.getUsersTable());
        new LeetSyncIngestionStack(app, "LeetSyncIngestionStack", dataStack.getAcSubmissionsTable(), dataStack.getUsersTable());
        new LeetSyncProblemStack(app, "LeetSyncProblemStack", dataStack.getProblemsTable());
        new LeetSyncEtlStack(app, "LeetSyncEtlStack", dataStack.getAcSubmissionsTable(), dataStack.getProblemsTable(), dataStack.getParquetBucket());
        new LeetSyncAnalyticsStack(app, "LeetSyncAnalyticsStack", dataStack.getParquetBucket(), dataStack.getAthenaResultsBucket(), dataStack.getUserStatsCacheTable());

        app.synth();
    }

}


