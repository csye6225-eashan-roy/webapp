# Cloud Native Web Application
CSYE 6225

### Assignment 1
Start Spring Boot application  

```
curl -vvvv http://localhost:8081/healthz
```  
(expected: 200 status ok)  

```
curl -vvvv http://localhost:8081/healthz?key=value
```  
(expected: 400 bad request)  

```
curl -vvvv -X GET http://localhost:8081/healthz --data-binary '{"key":"value"}' -H "Content-Type: application/json"
```  
(expected: 400 bad request)

```
curl -vvvv -XPUT http://localhost:8081/healthz
```  
(expected: 405 method not allowed)  

```
net stop postgresql-x64-16
curl -vvvv http://localhost:8081/healthz
```
(expected: 503 service unavailable)  

```
net start postgresql-x64-16
curl -vvvv http://localhost:8081/healthz
```
(expected: 200 status ok)

Notes:
- Decreased the time it takes to recognize a non-operational database to 5 secs via HikariCP 
