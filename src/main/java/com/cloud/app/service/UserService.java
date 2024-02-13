package com.cloud.app.service;

import com.cloud.app.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.cloud.app.entity.User;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(User user, String unencodedPassword) {

        // Checks if username is provided
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username must be provided");
        }

        // Checks if user is already present. If yes, throw exception.
        if (userDao.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("A user with the given username already exists");
        }

        // Prevent client from setting accountCreated and accountUpdated
        if (user.getAccountCreated() != null || user.getAccountUpdated() != null) {
            throw new IllegalArgumentException("Cannot set account created or updated time");
        }

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(unencodedPassword));

        user.setAccountCreated(Instant.now());
        user.setAccountUpdated(Instant.now());

        return userDao.save(user);
    }

    public User findByUserName(String username) {
        // Fetch and return the user by username
        Optional<User> user = userDao.findByUsername(username);
        return user.orElse(null);
        // Return the user if found, else return null
    }

    public User updateUser(String username, User userUpdateInfo) {
        // Find the existing user by username
        User existingUser = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check for unauthorized field updates
        if (isAttemptingToUpdateRestrictedFields(existingUser, userUpdateInfo)) {
            throw new IllegalArgumentException("Attempt to update unauthorized fields.");
        }

        // Update user details
        if (userUpdateInfo.getFirstName() != null) existingUser.setFirstName(userUpdateInfo.getFirstName());
        if (userUpdateInfo.getLastName() != null) existingUser.setLastName(userUpdateInfo.getLastName());
        if (userUpdateInfo.getPassword() != null && !userUpdateInfo.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userUpdateInfo.getPassword()));
        }
        existingUser.setAccountUpdated(Instant.now());

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
