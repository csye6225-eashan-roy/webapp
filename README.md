# Cloud Native Web Application
## CSYE 6225

### Assignment 1
Start Spring Boot application  

`curl -vvvv http://localhost:8080/healthz`  
(expected: returns 200 status ok)  

`curl -vvvv -XPUT http://localhost:8080/healthz`  
(expected: return 405 method not allowed)  

`net stop postgresql-x64-16`

Notes:
- Decreased the time it takes to recognize a non-operational database to 5 secs via HikariCP 
