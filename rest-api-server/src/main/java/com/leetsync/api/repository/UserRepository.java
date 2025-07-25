package com.leetsync.api.repository;

import com.leetsync.api.model.User;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing Users table using Enhanced Client with annotated models
 * Each service owns its models - true microservices pattern
 */
@Repository
public class UserRepository {

    private final DynamoDbTable<User> table;

    public UserRepository(DynamoDbEnhancedClient enhancedClient) {
        // Use annotated model directly - no static schema needed
        String tableName = System.getenv("USERS_TABLE_NAME");
        if (tableName == null) {
            tableName = "Users"; // Default fallback for local development
        }
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(User.class));
    }

    public List<User> findAll() {
        return table.scan().items().stream().toList();
    }

    public Optional<User> findByUsername(String username) {
        Key key = Key.builder().partitionValue(username).build();
        User user = table.getItem(key);
        return Optional.ofNullable(user);
    }

    public User save(User user) {
        table.putItem(user);
        return user;
    }

    public void deleteByUsername(String username) {
        Key key = Key.builder().partitionValue(username).build();
        table.deleteItem(DeleteItemEnhancedRequest.builder().key(key).build());
    }
}