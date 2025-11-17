# Build and Run Guide - Modernized Application

## Overview

This guide shows how to build and run the **modernized** Message Service application running on JDK 17 with Spring Boot 3.3.5.

## Prerequisites

- **JDK 17 or higher** (verify with `java -version`)
- **Maven 3.6+** (verify with `mvn -version`)
- **Git** (for cloning)

## Quick Start

### 1. Build the Application

```bash
# Clean and compile
mvn clean compile

# Run tests (when available)
mvn test

# Package as JAR
mvn clean package
```

The build produces: `target/message-service.jar`

### 2. Run the Application

**Option A: Using Maven**
```bash
mvn spring-boot:run
```

**Option B: Using JAR**
```bash
java -jar target/message-service.jar
```

**Option C: Using Docker**
```bash
# Build Docker image
docker build -t message-service:latest .

# Run container
docker run -p 8080:8080 message-service:latest
```

### 3. Access the Application

- **API Base URL**: http://localhost:8080/api/messages
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/api/messages/stats

## API Endpoints

### Get All Messages
```bash
curl http://localhost:8080/api/messages
```

**Response:**
```json
{
  "status": "success",
  "data": [...],
  "count": 5,
  "timestamp": "2025-11-17 15:45:00"
}
```

### Get Message by ID
```bash
curl http://localhost:8080/api/messages/1
```

### Create New Message
```bash
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Testing Spring Boot 3.x with JDK 17!",
    "author": "developer"
  }'
```

**Response (HTTP 201 Created):**
```json
{
  "status": "success",
  "message": "Message created successfully",
  "data": {
    "id": 6,
    "content": "Testing Spring Boot 3.x with JDK 17!",
    "author": "developer",
    "createdDate": "2025-11-17T15:45:30.123456",
    "updatedDate": null,
    "active": true
  },
  "createdAt": "2025-11-17 15:45:30"
}
```

### Update Message
```bash
curl -X PUT http://localhost:8080/api/messages/1 \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Updated message content"
  }'
```

### Delete Message
```bash
curl -X DELETE http://localhost:8080/api/messages/1
```

### Search Messages
```bash
curl "http://localhost:8080/api/messages/search?keyword=Spring"
```

### Get Messages by Author
```bash
curl http://localhost:8080/api/messages/author/admin
```

### Get Statistics
```bash
curl http://localhost:8080/api/messages/stats
```

**Response:**
```json
{
  "totalMessages": 5,
  "activeMessages": 5,
  "timestamp": "2025-11-17 15:45:00"
}
```

## Scheduled Task Verification

The application includes a scheduled task that runs **every 60 seconds**. You'll see this in the console logs:

```
2025-11-17T15:45:00.000Z  INFO 12345 --- [   scheduling-1] c.nytour.demo.task.MessageScheduledTask  : ========================================
2025-11-17T15:45:00.000Z  INFO 12345 --- [   scheduling-1] c.nytour.demo.task.MessageScheduledTask  : Message Statistics Task - Executing
2025-11-17T15:45:00.000Z  INFO 12345 --- [   scheduling-1] c.nytour.demo.task.MessageScheduledTask  : ========================================
2025-11-17T15:45:00.001Z  INFO 12345 --- [   scheduling-1] c.nytour.demo.task.MessageScheduledTask  : Execution Time: 2025-11-17 15:45:00
2025-11-17T15:45:00.010Z  INFO 12345 --- [   scheduling-1] c.nytour.demo.task.MessageScheduledTask  : Total Messages: 5
2025-11-17T15:45:00.010Z  INFO 12345 --- [   scheduling-1] c.nytour.demo.task.MessageScheduledTask  : Active Messages: 5
2025-11-17T15:45:00.010Z  INFO 12345 --- [   scheduling-1] c.nytour.demo.task.MessageScheduledTask  : Inactive Messages: 0
2025-11-17T15:45:00.020Z  INFO 12345 --- [   scheduling-1] c.nytour.demo.task.MessageScheduledTask  : Messages from last 7 days: 5
2025-11-17T15:45:00.020Z  INFO 12345 --- [   scheduling-1] c.nytour.demo.task.MessageScheduledTask  : Next Execution: 2025-11-17 15:46:00
2025-11-17T15:45:00.020Z  INFO 12345 --- [   scheduling-1] c.nytour.demo.task.MessageScheduledTask  : Task Status Code: 200
2025-11-17T15:45:00.020Z  INFO 12345 --- [   scheduling-1] c.nytour.demo.task.MessageScheduledTask  : Task completed successfully
2025-11-17T15:45:00.020Z  INFO 12345 --- [   scheduling-1] c.nytour.demo.task.MessageScheduledTask  : ========================================
```

## H2 Database Console

The application uses an H2 in-memory database. You can access the console at:

**URL**: http://localhost:8080/h2-console

**Connection Settings:**
- **JDBC URL**: `jdbc:h2:mem:messagedb`
- **Username**: `sa`
- **Password**: (leave empty)

## Configuration

### Application Properties

Located at: `src/main/resources/application.properties`

```properties
# Server Configuration
server.port=8080

# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:messagedb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Logging
logging.level.root=INFO
logging.level.com.nytour.demo=DEBUG
```

### Environment Variables

You can override configuration using environment variables:

```bash
# Change server port
export SERVER_PORT=9090
mvn spring-boot:run

# Change log level
export LOGGING_LEVEL_ROOT=DEBUG
mvn spring-boot:run
```

## Docker Deployment

### Build Image
```bash
docker build -t message-service:latest .
```

### Run Container
```bash
docker run -d \
  --name message-service \
  -p 8080:8080 \
  message-service:latest
```

### View Logs
```bash
docker logs -f message-service
```

### Stop Container
```bash
docker stop message-service
docker rm message-service
```

### Run with Custom Configuration
```bash
docker run -d \
  --name message-service \
  -p 9090:8080 \
  -e SERVER_PORT=8080 \
  -e LOGGING_LEVEL_ROOT=DEBUG \
  message-service:latest
```

## Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Build Failures
```bash
# Clean Maven cache
mvn clean

# Force update dependencies
mvn clean install -U

# Skip tests if needed
mvn clean package -DskipTests
```

### Application Won't Start
1. Verify JDK 17+ is installed: `java -version`
2. Check port 8080 is available
3. Review logs for error messages
4. Ensure H2 database can be created in memory

## Performance Tips

### JVM Tuning
```bash
# For production, set memory limits
java -Xms512m -Xmx1024m -jar target/message-service.jar

# For containers
docker run -d \
  --name message-service \
  -p 8080:8080 \
  -e JAVA_OPTS="-Xms512m -Xmx1024m" \
  message-service:latest
```

### Development Mode
```bash
# Enable Spring Boot DevTools
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Hot reload enabled automatically
```

## Testing the Migration

To verify the migration was successful:

1. **Application Starts**: Should start in ~3 seconds
2. **All Endpoints Work**: Test all CRUD operations
3. **Scheduled Task Runs**: Check logs for 60-second interval
4. **Database Functions**: Create, read, update, delete messages
5. **No Errors**: No stack traces or warnings in logs

## Next Steps

- Deploy to Azure Container Apps (see `AZURE_DEPLOYMENT.md`)
- Add unit and integration tests
- Implement authentication with Spring Security
- Add API documentation with Swagger/OpenAPI
- Migrate to persistent database (PostgreSQL/MySQL)

## Support

For issues or questions:
- Review `MIGRATION_SUMMARY.md` for migration details
- Check `AZURE_DEPLOYMENT.md` for Azure deployment
- Review Spring Boot 3.x documentation
- Check application logs for detailed error messages
