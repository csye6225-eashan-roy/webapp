package com.cloud.app.service;

import com.cloud.app.dao.UserDao;
import com.cloud.app.error.EmailNotVerifiedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.cloud.app.entity.User;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PubSubTemplate pubSubTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final ObjectMapper objectMapper = new ObjectMapper(); // For converting objects to JSON strings

    public User createUser(User user, String unencodedPassword) {
        LOGGER.debug("Creating new user with username: {}", user.getUsername());

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

        // a7-start
        // Sets email verification fields
        user.setEmailVerified(false);
//        user.setEmailVerificationToken(UUID.randomUUID().toString());
//        user.setEmailVerificationTokenExpiry(Instant.now().plus(Duration.ofMinutes(2)));
        User savedUser = userDao.save(user);
        LOGGER.info("User successfully created with username: {}", user.getUsername());

        // Publish a message to the Pub/Sub topic for email verification
        try {
            LOGGER.debug("Proceeding to publish user info to pub/sub for user: {}", savedUser.getUsername());
            String payload = objectMapper.writeValueAsString(new VerificationPayload(
                    savedUser.getUsername()
            ));
            pubSubTemplate.publish("verify_email", payload);
            LOGGER.info("Published verification message to topic: verify_email for user: {}", savedUser.getUsername());
        } catch (JsonProcessingException e) {
            LOGGER.error("Error while serializing verification payload for user: {}", savedUser.getUsername(), e);
        }
        return savedUser;
    }

    // Inner class to represent the verification payload
    @Data
    @AllArgsConstructor
    private static class VerificationPayload {
        private String username;
//        private String verificationToken;
    }
    // a7-end
    // a7-start
    public User findByUserName(String username) {
        LOGGER.debug("Fetching user with username: {}", username);
        Optional<User> userOpt = userDao.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Check if the user's email is verified
            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                LOGGER.info("User found with username: {} and email is verified.", username);
                return user;
            } else {
                // Email not verified
                LOGGER.warn("Email not verified for username: {}", username);
                throw new EmailNotVerifiedException("Email not verified for username: " + username);
            }
        } else {
            // User not found
            LOGGER.warn("No user found with username: {}", username);
            return null; // Or consider throwing a specific exception for user not found
        }
    }
    // a7-end

    public User updateUser(String username, User userUpdateInfo) {
        // Find the existing user by username

        LOGGER.debug("Updating user information for username: {}", username);

        User existingUser = userDao.findByUsername(username)
                .orElseThrow(() -> {
                    LOGGER.error("User not found for username: {}", username);
                    return new IllegalArgumentException("User not found");
                });
        // a7-start
        // Check if user's email is verified before allowing update
        if (!Boolean.TRUE.equals(existingUser.getEmailVerified())) {
            throw new EmailNotVerifiedException("Email not verified for username: " + username);
        }
        // a7-end

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
    // a7-start
    public boolean verifyUserEmail(String token) {
        Optional<User> userOptional = userDao.findByEmailVerificationToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getEmailVerificationTokenExpiry().isAfter(Instant.now())) {
                user.setEmailVerified(true);
                user.setEmailVerificationToken(null); // Clear the token after verification
                user.setEmailVerificationTokenExpiry(null);
                userDao.save(user);
                return true;
            }
        }
        return false;
    }
    // a7-end
}
