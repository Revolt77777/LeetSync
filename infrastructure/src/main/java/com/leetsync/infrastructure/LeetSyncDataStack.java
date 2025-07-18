package com.leetsync.infrastructure;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.constructs.Construct;

public class LeetSyncDataStack extends Stack {

    private final Table acSubmissionsTable;
    private final Table problemsTable;
    private final Table usersTable;

    public LeetSyncDataStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public LeetSyncDataStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // AcSubmissions Table - Primary data store
        this.acSubmissionsTable = Table.Builder.create(this, "AcSubmissionsTable")
                .tableName("AcSubmissions")
                .partitionKey(Attribute.builder()
                        .name("username")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("timestamp")
                        .type(AttributeType.NUMBER)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        // Add GSI for problem-based queries
        acSubmissionsTable.addGlobalSecondaryIndex(GlobalSecondaryIndexProps.builder()
                .indexName("titleSlug-timestamp-index")
                .partitionKey(Attribute.builder()
                        .name("titleSlug")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("timestamp")
                        .type(AttributeType.NUMBER)
                        .build())
                .build());

        // Problems Table - LeetCode problem catalog
        this.problemsTable = Table.Builder.create(this, "ProblemsTable")
                .tableName("Problems")
                .partitionKey(Attribute.builder()
                        .name("titleSlug")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        // Users Table - User management
        this.usersTable = Table.Builder.create(this, "UsersTable")
                .tableName("Users")
                .partitionKey(Attribute.builder()
                        .name("username")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
    }

    public Table getAcSubmissionsTable() {
        return acSubmissionsTable;
    }
    
    public Table getProblemsTable() {
        return problemsTable;
    }
    
    public Table getUsersTable() {
        return usersTable;
    }
}