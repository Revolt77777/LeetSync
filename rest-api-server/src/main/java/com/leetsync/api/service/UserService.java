package com.leetsync.api.service;

import com.leetsync.api.repository.ApiRepository;
import com.leetsync.api.repository.UserRepository;
import com.leetsync.shared.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ApiRepository apiRepository;

    public UserService(UserRepository userRepository, ApiRepository apiRepository) {
        this.userRepository = userRepository;
        this.apiRepository = apiRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(String username) {
        long now = System.currentTimeMillis();
        User user = new User(username, now, 0);
        return userRepository.save(user);
    }

    public void deleteUser(String username) {
        // Cascade delete: remove all user's submissions first
        apiRepository.deleteAllByUsername(username);
        // Then delete the user
        userRepository.deleteByUsername(username);
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}