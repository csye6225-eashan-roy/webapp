package com.cloud.app.service;

import com.cloud.app.dao.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.cloud.app.entity.User;
import java.time.Instant;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public User createUser(User user, String unencodedPassword) {
        LOGGER.info("Creating new user with username: {}", user.getUsername());

        // Checks if username is provided
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            LOGGER.warn("Attempted to create a user without a username");
            throw new IllegalArgumentException("Username must be provided");
        }

        // Checks if user is already present. If yes, throw exception.
        if (userDao.findByUsername(user.getUsername()).isPresent()) {
            LOGGER.warn("Attempted to create a user that already exists: {}", user.getUsername());
            throw new IllegalArgumentException("A user with the given username already exists");
        }

        // Prevent client from setting accountCreated and accountUpdated
        if (user.getAccountCreated() != null || user.getAccountUpdated() != null) {
            LOGGER.warn("Attempted to set account created or updated time for username: {}", user.getUsername());
            throw new IllegalArgumentException("Cannot set account created or updated time");
        }

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(unencodedPassword));
        user.setAccountCreated(Instant.now());
        user.setAccountUpdated(Instant.now());

        LOGGER.info("User successfully created with username: {}", user.getUsername());
        return userDao.save(user);
    }

    public User findByUserName(String username) {
        // Fetch and return the user by username

        LOGGER.info("Fetching user with username: {}", username);
        Optional<User> user = userDao.findByUsername(username);

        //return user.orElse(null);
        // Return the user if found, else return null
        if (user.isPresent()) {
            LOGGER.info("User found with username: {}", username);
            return user.get();
        } else {
            LOGGER.warn("No user found with username: {}", username);
            return null;
        }
    }

    public User updateUser(String username, User userUpdateInfo) {
        // Find the existing user by username

        LOGGER.info("Updating user information for username: {}", username);

        User existingUser = userDao.findByUsername(username)
                .orElseThrow(() -> {
                    LOGGER.error("User not found for username: {}", username);
                    return new IllegalArgumentException("User not found");
                });

        // Check for unauthorized field updates
        if (isAttemptingToUpdateRestrictedFields(existingUser, userUpdateInfo)) {
            LOGGER.warn("Attempted to update restricted fields for username: {}", username);
            throw new IllegalArgumentException("Attempt to update unauthorized fields.");
        }

        // Update user details
        boolean detailsUpdated = false;

        if (userUpdateInfo.getFirstName() != null) {
            existingUser.setFirstName(userUpdateInfo.getFirstName());
            detailsUpdated = true;
        }
        if (userUpdateInfo.getLastName() != null) {
            existingUser.setLastName(userUpdateInfo.getLastName());
            detailsUpdated = true;
        }
        if (userUpdateInfo.getPassword() != null && !userUpdateInfo.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userUpdateInfo.getPassword()));
            detailsUpdated = true;
        }

        if (detailsUpdated) {
            existingUser.setAccountUpdated(Instant.now());
            LOGGER.info("User details updated for username: {}", username);
        } else {
            LOGGER.info("No updateable details provided for username: {}", username);
        }
        // Save the updated user
        return userDao.save(existingUser);
    }

    private boolean isAttemptingToUpdateRestrictedFields(User existingUser,User userUpdateInfo) {
        boolean updatesRestrictedFields = false;
        if (userUpdateInfo.getAccountUpdated() != null
                || userUpdateInfo.getAccountCreated() != null
                || userUpdateInfo.getId() != null
                || (userUpdateInfo.getUsername() != null)) {
            updatesRestrictedFields = true;
        }
        return updatesRestrictedFields;
    }
}
