# Cloud Native Web Application
CSYE 6225

### Assignment 1
- Start Spring Boot application  


- GET request with no payload, no query params
```
curl -vvvv http://localhost:8081/healthz
```  
[expected: 200 status ok]  

- GET request with query params
```
curl -vvvv http://localhost:8081/healthz?key=value
```  
[expected: 400 bad request]  

- GET request with payload
```
curl -vvvv -X GET http://localhost:8081/healthz --data-binary '{"key":"value"}' -H "Content-Type: application/json"
```  
[expected: 400 bad request]

- PUT/POST/PATCH/DELETE request
```
curl -vvvv -XPUT http://localhost:8081/healthz
```  
[expected: 405 method not allowed]  

- Stop Postgresql, make GET request
```
net stop postgresql-x64-16
curl -vvvv http://localhost:8081/healthz
```
[expected: 503 service unavailable]  

- Start Postgresql, make GET request
```
net start postgresql-x64-16
curl -vvvv http://localhost:8081/healthz
```
[expected: 200 status ok]

Notes:
- Decreased the time it takes to recognize a non-operational database to 5 secs via HikariCP  

### Assignment 2  

- Bootstrapping the Database  

-- Automated Database Setup: Leveraging the **CommandLineRunner** interface, our application is equipped to automatically establish a database along with a designated user endowed with the necessary privileges. This setup ensures that, in the event of an unexpected deletion, the database is seamlessly regenerated without manual intervention.  

-- Schema Management: Our use of JPA entity classes, in combination with Spring Data JPA repositories, facilitates the auto-generation of database tables. This negates the need for manual SQL query execution to restore tables should they be dropped.  

- REST APIs  

-- POST request to create user  

Endpoint:
```
http://localhost:8081/v1/user
```
Request Body:
```
{
  "first_name": "Jane",
  "last_name": "Doe",
  "password": "skdjfhskdfjhg@1A",
  "username": "jane.doe@example.com"
}
```
Responses:
```
- 201 created
{
  "id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "first_name": "Jane",
  "last_name": "Doe",
  "username": "jane.doe@example.com",
  "account_created": "2016-08-29T09:12:33.001Z",
  "account_updated": "2016-08-29T09:12:33.001Z"
}
- 400 bad request 
```
-- GET request to fetch user details  
(basic authentication token needed to make API call) 

Endpoint:
```
http://localhost:8081/v1/user/self
```  
Responses:
```
- 200 status ok
{
  "id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "first_name": "Jane",
  "last_name": "Doe",
  "username": "jane.doe@example.com",
  "account_created": "2016-08-29T09:12:33.001Z",
  "account_updated": "2016-08-29T09:12:33.001Z"
}
- 401 unauthorized
```
-- PUT request to update user details  
  (basic authentication token needed to make API call)

Endpoint:
```
http://localhost:8081/v1/user/self
{
  "first_name": "Jane",
  "last_name": "Doe",
  "password": "skdjfhskdfjhg"
}
```  
Responses:
```
- 204 no content
- 400 bad request
- 401 unauthorized
```

- GitHub actions CI Pipeline  

-- Wrote a GitHub Actions workflow in yml to run simple check (compile code) for each pull request raised.   
-- A pull request can only be merged if the workflow executes successfully.  


- Bash script to demo assignment-2 in a centos vm  

-- The script installs necessary packages like unzip, JDK, maven, postgresql in the vm      

-- Unzips the project folder, creates directory 'resources' under webapp/src/main, and places application.properties file inside it    

-- Initializes and starts PostgreSQL  

-- Configures PostgreSQL  

-- Configures md5 authentication in pg_hba.conf  

-- Makes maven point to JDK 17 instead of the default mapping to JDK 11  

-- Builds and runs the Spring Boot application  

### Assignment 3

- Added Integration tests  

-- Test 1 - Created an account, and using the GET call, validated account exists  
-- Test 2 - Updated the account and using the GET call, validated the account was updated 


- Added GitHub secrets to encrypt sensitive information in CI workflow  

-- Created secrets in organization repo  
-- Enabled 'Run workflows from fork pull requests' and 'Send secrets and variables to workflows from fork pull requests' in order to give access of secrets to fork repo  
-- Accessed the secrets in workflow file through ${{ secrets.VARIABLE_NAME }}  

### Assignment 4
