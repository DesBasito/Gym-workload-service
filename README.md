# Workload Service

A microservice for tracking trainer monthly workload in the Gym CRM System.

## Description

Workload Service accepts information about trainings (add/delete) and calculates the total working hours for each trainer by month. Data is stored in an in-memory structure for fast access.

## Features

- ✅ REST API for adding/deleting trainer workload
- ✅ In-memory data storage (ConcurrentHashMap)
- ✅ JWT Bearer Token authorization
- ✅ Eureka Discovery Service registration
- ✅ Two-level logging (Transaction + Operation)
- ✅ Circuit Breaker (Resilience4j)
- ✅ Actuator endpoints for monitoring
- ✅ Prometheus metrics

## Tech Stack

- Java 17
- Spring Boot 3.5.3
- Spring Cloud (Eureka Client)
- Spring Security + JWT
- Resilience4j
- Lombok
- SLF4J/Logback

## Project Structure

```
workload-service/
├── src/main/java/abu/epam/com/workloadservice/
│   ├── config/          # Configuration (Security)
│   ├── controller/      # REST Controllers
│   ├── dto/             # Data Transfer Objects
│   ├── filter/          # Filters (Logging, Transaction)
│   ├── model/           # Data Models
│   ├── security/        # JWT components
│   └── service/         # Business Logic
└── src/main/resources/
    ├── application.yml      # Application configuration
    └── logback-spring.xml   # Logging configuration
```

## API Endpoints

### 1. Update Trainer Workload
```http
POST /api/workload
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "username": "john.doe",
  "firstName": "John",
  "lastName": "Doe",
  "isActive": true,
  "trainingDate": "2024-02-08",
  "trainingDuration": 60,
  "actionType": "ADD"
}
```

**Field Descriptions:**
- `username` - Trainer's unique username
- `firstName` - Trainer's first name
- `lastName` - Trainer's last name
- `isActive` - Trainer's active status
- `trainingDate` - Training date
- `trainingDuration` - Training duration (in minutes)
- `actionType` - Action type: `ADD` or `DELETE`

**Response:**
- `200 OK` - Workload updated successfully
- `400 Bad Request` - Validation error
- `401 Unauthorized` - Missing or invalid JWT token

### 2. Get Specific Trainer Workload
```http
GET /api/workload/{username}
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "username": "john.doe",
  "firstName": "John",
  "lastName": "Doe",
  "isActive": true,
  "years": {
    "2024": {
      "months": {
        "1": {
          "totalDuration": 120
        },
        "2": {
          "totalDuration": 180
        }
      }
    }
  }
}
```

### 3. Get All Trainers Workload
```http
GET /api/workload
Authorization: Bearer <JWT_TOKEN>
```

## Configuration

### Ports
- **Application**: `8082`
- **Eureka Server**: `8761` (default)

### JWT Secret
Default: `MySecretKeyForJWTTokenGenerationAndValidationPurpose12345`

To change, update in `application.yml`:
```yaml
jwt:
  secret: YOUR_SECRET_KEY
```

### Eureka Configuration
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## Running the Service

### Requirements
1. JDK 17+
2. Maven 3.6+
3. Eureka Server (should be running on port 8761)

### Build and Run
```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the JAR file
java -jar target/workload-service-0.0.1-SNAPSHOT.jar
```

## API Documentation

### Swagger UI
Access interactive API documentation at:
- **Swagger UI**: `http://localhost:8082/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8082/v3/api-docs`

The Swagger UI provides:
- Interactive API testing
- Request/response examples
- Schema definitions
- JWT authentication testing (click "Authorize" button and enter your JWT token)

## Monitoring

### Actuator Endpoints
- Health: `http://localhost:8082/actuator/health`
- Metrics: `http://localhost:8082/actuator/metrics`
- Prometheus: `http://localhost:8082/actuator/prometheus`

## Logging

### Two-Level Logging

#### Transaction Level
HTTP request/response logging with unique `transactionId` generation:
```
2024-02-08 10:30:45.123 [http-nio-8082-exec-1] INFO  [TxId: abc-123-def] [TRANSACTION] START - Method: POST, URI: /api/workload
2024-02-08 10:30:45.456 [http-nio-8082-exec-1] INFO  [TxId: abc-123-def] [TRANSACTION] END - Status: 200, Duration: 333ms
```

#### Operation Level
Detailed logging of operations within the transaction:
```
2024-02-08 10:30:45.234 [http-nio-8082-exec-1] DEBUG [TxId: abc-123-def] [OPERATION] Request Body: {...}
2024-02-08 10:30:45.345 [http-nio-8082-exec-1] INFO  [TxId: abc-123-def] Processing workload for trainer: john.doe
```

### Log Files
Logs are saved to:
- Console output
- `logs/workload-service.log`
- Rotation: daily, retention 30 days

## Circuit Breaker

Resilience4j Circuit Breaker configured with:
- Sliding Window Size: 10 requests
- Failure Rate Threshold: 50%
- Wait Duration in Open State: 5 seconds
- Minimum Number of Calls: 5

## Integration with Gym CRM System

Workload Service is designed to be called from the main Gym-CRM-system when:
- Creating a new training (actionType: ADD)
- Deleting a training (actionType: DELETE)

## Security

### JWT Authentication
All API endpoints (except actuator) are protected with JWT authentication.

For access:
1. Obtain JWT token from the main Gym-CRM-system
2. Add token to header: `Authorization: Bearer <token>`

## Usage Examples

### cURL
```bash
# Add training
curl -X POST http://localhost:8082/api/workload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "firstName": "John",
    "lastName": "Doe",
    "isActive": true,
    "trainingDate": "2024-02-08",
    "trainingDuration": 60,
    "actionType": "ADD"
  }'

# Get trainer workload
curl -X GET http://localhost:8082/api/workload/john.doe \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Troubleshooting

### Service not registering with Eureka
1. Check that Eureka Server is running on `localhost:8761`
2. Check logs for connection errors
3. Ensure `eureka.client.register-with-eureka=true`

### JWT token validation fails
1. Ensure you're using the correct secret key
2. Check token format: `Bearer <token>`
3. Verify token expiration

### Data not persisting
The service uses in-memory storage. All data is lost on restart.
This is expected behavior according to requirements.

## License

Developed as part of EPAM Specialization Program.