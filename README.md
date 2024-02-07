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
-