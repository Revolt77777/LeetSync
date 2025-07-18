package com.leetsync.api.controller;

import com.leetsync.api.service.UserService;
import com.leetsync.shared.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{username}")
    public ResponseEntity<User> createUser(@PathVariable String username) {
        if (userService.userExists(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User createdUser = userService.createUser(username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        if (!userService.userExists(username)) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }
}