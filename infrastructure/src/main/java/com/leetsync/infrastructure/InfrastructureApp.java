package com.leetsync.infrastructure;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

public class InfrastructureApp {
    public static void main(final String[] args) {
        App app = new App();

        // Create data stack first
        LeetSyncDataStack dataStack = new LeetSyncDataStack(app, "LeetSyncDataStack");
        
        // Pass table references to other stacks
        new LeetSyncApiStack(app, "LeetSyncApiStack", dataStack.getAcSubmissionsTable(), dataStack.getUsersTable());
        new LeetSyncIngestionStack(app, "LeetSyncIngestionStack", dataStack.getAcSubmissionsTable(), dataStack.getUsersTable());
        new LeetSyncProblemStack(app, "LeetSyncProblemStack", dataStack.getProblemsTable());

        app.synth();
    }

}


