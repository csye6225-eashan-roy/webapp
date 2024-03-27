package com.cloud.app.controller;

import com.cloud.app.dto.UserDTO;
import com.cloud.app.entity.User;
import com.cloud.app.service.HealthCheckService;
import com.cloud.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private HealthCheckService healthCheckService;

    private final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    // User creation
    @PostMapping("/user")
    public ResponseEntity<?> createUser(@Validated @RequestBody User user) {
        LOGGER.info("Request received to create user: {}", user.getUsername());

        try {
            LOGGER.debug("Proceeding to create user...");
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(user.getUsername());
            userDTO.setLastName(user.getLastName());
            userDTO.setFirstName(user.getFirstName());
            userDTO.setAccountCreated(user.getAccountCreated());
            userDTO.setAccountUpdated(user.getAccountUpdated());
            User createdUser = userService.createUser(userDTO.toEntity(), user.getPassword());
            LOGGER.info("User successfully created: {}", userDTO.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(UserDTO.fromEntity(createdUser));

        } catch (DataAccessResourceFailureException e){
            LOGGER.error("Database service unavailable. Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
        catch (Exception e) {
            LOGGER.error("Error creating user '{}': {}", user.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Current user can get their information
    @GetMapping("/user/self")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal UserDetails currentUser,
                                         HttpServletRequest payload,
                                         @RequestParam Map<String,String> queryParams) {
        LOGGER.info("Request received to retrieve user info for: {}", currentUser.getUsername());

        if(payload.getContentLength()>0 || !queryParams.isEmpty()){
            LOGGER.warn("Bad request for user info retrieval: {}, with payload or query parameters present", currentUser.getUsername());
            return ResponseEntity
                    .badRequest()
                    .header("Cache-control","no-cache, no-store, must-revalidate")
                    .header("Pragma","no-cache")
                    .header("X-Content-Type-Options","nosniff")
                    .build();
        }

        User user = userService.findByUserName(currentUser.getUsername());
        LOGGER.debug("Proceeding to get user info...");
        if (user != null) {
            LOGGER.info("User info successfully retrieved for: {}", currentUser.getUsername());
            return ResponseEntity.ok(UserDTO.fromEntity(user));
        } else {
            LOGGER.warn("User not found for username: {}", currentUser.getUsername());
            return ResponseEntity.notFound().build();
        }
    }

    // Current user can update their information
    @PutMapping("/user/self")
    public ResponseEntity<?> updateUserInformation(@AuthenticationPrincipal UserDetails currentUser, @Validated @RequestBody User user) {
        LOGGER.info("Request received to update user info for: {}", currentUser.getUsername());
        try {
            LOGGER.debug("Proceeding to update user info...");
            User updatedUser = userService.updateUser(currentUser.getUsername(), user);
            LOGGER.info("User information successfully updated for: {}", currentUser.getUsername());
            return ResponseEntity.noContent().build(); // 204 No Content

        } catch (Exception e) {
            LOGGER.error("Error updating user '{}': {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage()); // 400 Bad Request
        }
    }

    // a7-start
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        LOGGER.info("Request received to verify email id");
        if (userService.verifyUserEmail(token)) {
            return ResponseEntity.ok().body("Email verified successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email verification link has expired.");
        }
    }
    // a7-end
}
