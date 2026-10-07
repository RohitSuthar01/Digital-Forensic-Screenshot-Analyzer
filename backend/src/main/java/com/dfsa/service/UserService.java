package com.dfsa.service;

import com.dfsa.model.User;
import com.dfsa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public void incrementFailedAttempts(String username) {
        findByUsername(username).ifPresent(user -> {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= 5) {
                // Lock account for 15 minutes
                Date lockUntil = new Date(System.currentTimeMillis() + 15 * 60 * 1000);
                user.setLockUntil(lockUntil);
            }
            save(user);
        });
    }

    public void resetFailedAttempts(String username) {
        findByUsername(username).ifPresent(user -> {
            user.setFailedAttempts(0);
            user.setLockUntil(null);
            save(user);
        });
    }
}