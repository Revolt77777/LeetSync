package com.leetsync.api.repository;

import com.leetsync.shared.model.User;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private static final TableSchema<User> TABLE_SCHEMA =
            TableSchema.builder(User.class)
                    .newItemSupplier(User::new)
                    .addAttribute(String.class, a -> a.name("username")
                            .getter(User::getUsername)
                            .setter(User::setUsername)
                            .tags(StaticAttributeTags.primaryPartitionKey()))
                    .addAttribute(Long.class, a -> a.name("createdAt")
                            .getter(User::getCreatedAt)
                            .setter(User::setCreatedAt))
                    .addAttribute(Long.class, a -> a.name("lastSync")
                            .getter(User::getLastSync)
                            .setter(User::setLastSync))
                    .build();

    private final DynamoDbTable<User> table;

    public UserRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("Users", TABLE_SCHEMA);
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