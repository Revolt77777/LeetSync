package com.leetsync.infrastructure;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

public class InfrastructureApp {
    public static void main(final String[] args) {
        App app = new App();

        new LeetSyncApiStack(app, "LeetSyncApiStack");

        app.synth();
    }
}


