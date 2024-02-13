package com.cloud.app.controller;

import com.cloud.app.dto.UserDTO;
import com.cloud.app.entity.User;
import com.cloud.app.service.HealthCheckService;
import com.cloud.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private HealthCheckService healthCheckService;

    // User creation
    @PostMapping("/user")
    public ResponseEntity<?> createUser(@Validated @RequestBody User user) {

        try {
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(user.getUsername());
            userDTO.setLastName(user.getLastName());
            userDTO.setFirstName(user.getFirstName());
            userDTO.setAccountCreated(user.getAccountCreated());
            userDTO.setAccountUpdated(user.getAccountUpdated());
            User createdUser = userService.createUser(userDTO.toEntity(), user.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).body(UserDTO.fromEntity(createdUser));
        } catch (DataAccessResourceFailureException e){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Current user can get their information
    @GetMapping("/user/self")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal UserDetails currentUser,
                                         HttpServletRequest payload,
                                         @RequestParam Map<String,String> queryParams) {

//        if (!healthCheckService.isDatabaseRunning()) {
//            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
//        }

        if(payload.getContentLength()>0 || !queryParams.isEmpty()){
            return ResponseEntity
                    .badRequest()
                    .header("Cache-control","no-cache, no-store, must-revalidate")
                    .header("Pragma","no-cache")
                    .header("X-Content-Type-Options","nosniff")
                    .build();
        }

        User user = userService.findByUserName(currentUser.getUsername());
        if (user != null) {
            return ResponseEntity.ok(UserDTO.fromEntity(user));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Current user can update their information
    @PutMapping("/user/self")
    public ResponseEntity<?> updateUserInformation(@AuthenticationPrincipal UserDetails currentUser, @Validated @RequestBody User user) {
        try {
//            if (!healthCheckService.isDatabaseRunning()) {
//                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
//            }

            User updatedUser = userService.updateUser(currentUser.getUsername(), user);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // 400 Bad Request
        }
    }
}
