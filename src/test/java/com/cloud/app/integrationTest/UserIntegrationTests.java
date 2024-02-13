package com.cloud.app.integrationTest;

import com.cloud.app.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//selects a random port to conduct actual http calls for testing
public class UserIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;
    //will make actual HTTP calls as opposed to mockmvc which simulates them

    @LocalServerPort
    //this annotation is used to  inject the random port used at runtime into the port variable
    private int port;
    private String baseUrl;

    public String getRootUrl() {
        return "http://localhost:" + port + "/v1";
    }

    @Test
    public void testCreateAndGetUser() {
        // Objective: Creating a user via POST and retrieving it via GET

        User user = new User(); // Assuming a constructor without ID (auto-generated)
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUsername("test-user2@example.com");
        user.setPassword("Password123!@");

        // Create user
        ResponseEntity<User> postResponse = restTemplate.postForEntity(getRootUrl() + "/user", user, User.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        User createdUser = postResponse.getBody();

        // Assuming the username and password are for basic auth, not directly stored in the User entity as clear text.
        restTemplate = restTemplate.withBasicAuth(user.getUsername(), "Password123!@");

        // Retrieve user
        ResponseEntity<User> getResponse = restTemplate.getForEntity(getRootUrl() + "/user/self", User.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        User retrievedUser = getResponse.getBody();

        assertThat(retrievedUser.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void testUpdateUser() {
        // Updating a user via PUT and validating the update via GET

        // Using the same username and password that were used to create the user in testCreateAndGetUser()
        String username = "test-user2@example.com";
        String password = "Password123!@";

        // Configuring restTemplate with Basic Auth for the user created in testCreateAndGetUser()
        restTemplate = restTemplate.withBasicAuth(username, password);

        // Retrieving the user using authenticated GET request
        ResponseEntity<User> getResponse = restTemplate.getForEntity(getRootUrl() + "/user/self", User.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        User userToUpdate = getResponse.getBody();

        // Ensuring that we have retrieved a user before proceeding
        assertThat(userToUpdate).isNotNull();
        assertThat(userToUpdate.getUsername()).isEqualTo(username);

        // Updating the user's details
        userToUpdate.setLastName("user - updated");
        userToUpdate.setFirstName("Test");
        userToUpdate.setPassword("Password123!@");
        userToUpdate.setId(null);
        userToUpdate.setAccountCreated(null);
        userToUpdate.setAccountUpdated(null);
        userToUpdate.setUsername(null);
        // Prepare the request for updating the user
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> entity = new HttpEntity<>(userToUpdate, headers);

        // Sending a PUT request to update the user
        ResponseEntity<User> updateResponse = restTemplate.exchange(getRootUrl() + "/user/self", HttpMethod.PUT, entity, User.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Retrieving the updated user to verify the updates
        ResponseEntity<User> getUpdatedResponse = restTemplate.getForEntity(getRootUrl() + "/user/self", User.class);
        assertThat(getUpdatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        User updatedUser = getUpdatedResponse.getBody();

        // Asserting that the updates are applied
        assertThat(updatedUser.getLastName()).isEqualTo("user - updated");
    }
}
