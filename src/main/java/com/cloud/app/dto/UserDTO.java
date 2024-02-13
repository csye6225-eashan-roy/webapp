package com.cloud.app.dto;

import com.cloud.app.entity.User;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String username;

    @JsonProperty("account_created")
    private Instant accountCreated;

    @JsonProperty("account_updated")
    private Instant accountUpdated;

    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setAccountCreated(user.getAccountCreated());
        dto.setAccountUpdated(user.getAccountUpdated());
        return dto;
    }

    public User toEntity() {
        User user = new User();
        user.setFirstName(this.getFirstName());
        user.setLastName(this.getLastName());
        user.setUsername(this.getUsername());
        user.setAccountCreated(this.getAccountCreated());
        user.setAccountUpdated(this.getAccountUpdated());
        return user;
    }
}
