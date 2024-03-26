package com.cloud.app.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false)
    @JsonProperty("first_name")
    @NotBlank(message = "First name cannot be empty")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @JsonProperty("last_name")
    @NotBlank(message = "Last name cannot be empty")
    private String lastName;

    @Column(unique = true)
//    @NotBlank(message = "Username cannot be empty")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,}$",
            message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character")
    private String password;

    @Column(name = "account_created")
    @JsonProperty("account_created")
    private Instant accountCreated;

    @Column(name = "account_updated")
    @JsonProperty("account_updated")
    private Instant accountUpdated;

    // a7-start
    //for email verification
    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "email_verification_token_expiry")
    private Instant emailVerificationTokenExpiry;
    //a7-end
}
