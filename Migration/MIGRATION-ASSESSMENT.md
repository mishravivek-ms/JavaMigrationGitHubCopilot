# Migration Assessment: Spring Boot 2.7.x to Spring Boot 3.x

**Assessment Date**: November 19, 2025  
**Application**: Message Service Legacy  
**Current Version**: Spring Boot 2.7.18, JDK 1.8  
**Target Version**: Spring Boot 3.x, JDK 17 LTS

---

## Executive Summary

This document provides a comprehensive assessment for migrating the Message Service Legacy application from Spring Boot 2.7.18 (JDK 1.8) to Spring Boot 3.x (JDK 17). The application consists of a REST API, a scheduled task running every minute, and a JPA data layer using H2 in-memory database.

**Application Scope**:
- 6 Java files (~709 lines of code)
- 3 configuration files (pom.xml, application.properties, log4j.properties)
- 1 new file to create (Dockerfile)

**Key Findings**:
- Migration is **feasible** with moderate effort (29-45 hours / 4-6 days)
- Major breaking changes in package namespaces (javax → jakarta)
- Scheduled task requirement (every 60 seconds) can be preserved
- Recommended deployment: **Azure Container Apps** for cost-effectiveness and simplicity

---

## Table of Contents

1. [Migration Analysis](#1-migration-analysis)
2. [Azure Deployment Options](#2-azure-deployment-options)
3. [Recommended Approach](#3-recommended-approach)
4. [Code Change Examples](#4-code-change-examples)
5. [Effort Estimation](#5-effort-estimation)
6. [Risk Assessment](#6-risk-assessment)

---

## 1. Migration Analysis

### 1.1 Breaking Changes: Spring Boot 2.7 → 3.x

#### Critical Changes

| Area | Change | Impact | Files Affected |
|------|--------|--------|----------------|
| **Namespace** | javax.* → jakarta.* | HIGH | All Java files |
| **JDK Version** | 1.8 → 17 (minimum) | HIGH | pom.xml, runtime |
| **Hibernate** | 5.6.x → 6.x | MEDIUM | Entity classes |
| **Spring Framework** | 5.3.x → 6.x | MEDIUM | All Spring components |
| **Validation** | javax.validation → jakarta.validation | HIGH | DTOs, Entities |

#### Package Remapping Required

**All files must update these imports**:

```
javax.persistence.*      → jakarta.persistence.*
javax.validation.*       → jakarta.validation.*
javax.servlet.*          → jakarta.servlet.*
javax.transaction.*      → jakarta.transaction.*
```

**Affected Files**:
- `Message.java` - Entity with JPA annotations
- `MessageController.java` - Uses javax.servlet, javax.validation
- `MessageRepository.java` - JPA interfaces
- `MessageService.java` - Transaction annotations
- All DTO classes with validation

### 1.2 JDK 1.8 → 17 Compatibility Issues

#### Removed/Deprecated APIs

| Legacy API | Issue | Modern Alternative |
|------------|-------|-------------------|
| `new Integer(value)` | Removed in JDK 9+ | `Integer.valueOf(value)` |
| `new Boolean(value)` | Removed in JDK 9+ | `Boolean.valueOf(value)` |
| `finalize()` method | Deprecated, removed in JDK 18+ | Cleaners, try-with-resources |
| `java.util.Date` constructors | Deprecated | `java.time.LocalDateTime` |
| `SimpleDateFormat` | Not thread-safe | `java.time.DateTimeFormatter` |

#### Current Usage in Codebase

**MessageScheduledTask.java**:
- Line 76: `new Integer(200)` → Should use `Integer.valueOf(200)` or just `200`
- Lines 117-123: `finalize()` method → Should be removed
- Lines 9-11: `SimpleDateFormat`, `Calendar`, `Date` → Migrate to java.time

**Message.java**:
- Line 52, 60: `new Date()` → Should use `LocalDateTime.now()`
- Line 61: `new Boolean(true)` → Should use `Boolean.TRUE` or `true`

**MessageController.java**:
- Line 47: `SimpleDateFormat` (not thread-safe) → `DateTimeFormatter`
- Lines 16-17: `java.util.Date` → `java.time.LocalDateTime`

**MessageService.java**:
- Lines 10-12: `Calendar`, `Date` → `java.time` APIs

### 1.3 Spring Framework 5.x → 6.x Changes

#### Controller Patterns

**Legacy Pattern** (Spring Boot 2.7):
```java
@Controller
@RequestMapping("/api/messages")
public class MessageController {
    
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllMessages() {
        // ...
    }
}
```

**Modern Pattern** (Spring Boot 3.x):
```java
@RestController
@RequestMapping("/api/messages")
public class MessageController {
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMessages() {
        // ...
    }
}
```

**Changes Required**:
- Replace `@Controller + @ResponseBody` with `@RestController`
- Replace `@RequestMapping(method = RequestMethod.GET)` with `@GetMapping`
- Replace `@RequestMapping(method = RequestMethod.POST)` with `@PostMapping`
- Replace `@RequestMapping(method = RequestMethod.PUT)` with `@PutMapping`
- Replace `@RequestMapping(method = RequestMethod.DELETE)` with `@DeleteMapping`

#### Dependency Injection

**Legacy Pattern**:
```java
@Autowired
private MessageService messageService;
```

**Modern Pattern**:
```java
private final MessageService messageService;

public MessageController(MessageService messageService) {
    this.messageService = messageService;
}
```

**Benefits**:
- Immutable dependencies
- Easier testing
- Clear dependencies
- Null-safety

### 1.4 Deprecated API Usage

#### Log4j 1.x → SLF4J/Logback

**Current**: Uses Log4j 1.2.17 (deprecated, security vulnerabilities)

**Files Affected**:
- `MessageController.java` - Line 40
- `MessageScheduledTask.java` - Line 28

**Migration**:
```java
// Before
import org.apache.log4j.Logger;
private static final Logger logger = Logger.getLogger(MessageController.class);

// After
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
```

Spring Boot 3.x includes SLF4J with Logback by default, so no dependency changes needed.

#### Commons Lang 2.x → 3.x

**Current**: Uses commons-lang 2.6 (deprecated since 2011)

**File Affected**: `MessageService.java` - Line 5

**Migration**:
```java
// Before
import org.apache.commons.lang.StringUtils;

// After
import org.apache.commons.lang3.StringUtils;
```

**pom.xml change**:
```xml
<!-- Remove -->
<dependency>
    <groupId>commons-lang</groupId>
    <artifactId>commons-lang</artifactId>
    <version>2.6</version>
</dependency>

<!-- Add -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>
```

### 1.5 Hibernate 5.6.x → 6.x Changes

#### Type Annotations

**File**: `Message.java` - Line 46

**Issue**:
```java
@Type(type = "yes_no") // Hibernate 5.x specific
private Boolean active;
```

**Spring Boot 3 / Hibernate 6**:
The `@Type` annotation API has changed. For simple boolean types, just remove it:

```java
@Column(name = "is_active")
private Boolean active;
```

Hibernate 6 handles boolean-to-char mapping differently. If "yes_no" mapping is critical, use:
```java
@Convert(converter = org.hibernate.type.YesNoConverter.class)
private Boolean active;
```

### 1.6 Date/Time API Migration

#### java.util.Date → java.time.LocalDateTime

**Current Usage**:
- Entity fields: `Date createdDate`, `Date updatedDate`
- Service methods: `Calendar` arithmetic
- Controllers: `SimpleDateFormat` formatting
- Scheduled tasks: `Date` and `Calendar` manipulation

**Migration Strategy**:

```java
// Entity
@Column(name = "created_date", nullable = false)
private LocalDateTime createdDate;

// Pre-persist
@PrePersist
protected void onCreate() {
    createdDate = LocalDateTime.now();
}

// Service - Date arithmetic
// Before: Calendar.getInstance().add(Calendar.DAY_OF_MONTH, -7)
LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);

// Controller - Formatting
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
String formattedDate = LocalDateTime.now().format(formatter);
```

**Note**: Repository query methods need parameter type changes:
```java
// Before
List<Message> findRecentActiveMessages(@Param("date") Date date);

// After
List<Message> findRecentActiveMessages(@Param("date") LocalDateTime date);
```

---

## 2. Azure Deployment Options

### Option 1: Azure App Service + Azure Functions

#### Architecture
- **REST API**: Deploy Spring Boot app to Azure App Service (Java SE 17)
- **Scheduled Task**: Extract to Azure Functions with Timer Trigger

#### Implementation Details

**App Service Configuration**:
- Runtime: Java 17
- Web Server: Embedded Tomcat (from Spring Boot)
- Deployment: Maven plugin or GitHub Actions
- Scaling: Vertical (scale up) or Horizontal (scale out)

**Azure Functions Configuration**:
- Trigger: `@TimerTrigger(name = "timer", schedule = "0 */1 * * * *")` (every minute)
- Runtime: Java 17
- Shared storage: Azure Storage Account or Azure SQL for message data
- Communication: REST API calls or shared database

**Sample Function Code**:
```java
@FunctionName("MessageStatistics")
public void run(
    @TimerTrigger(name = "timer", schedule = "0 */1 * * * *") String timerInfo,
    ExecutionContext context) {
    
    context.getLogger().info("Scheduled task executing at: " + LocalDateTime.now());
    // Call App Service REST API or query database directly
}
```

#### Pros
✅ Fully managed PaaS - No infrastructure management  
✅ Familiar deployment model for traditional Java teams  
✅ Built-in monitoring (Application Insights)  
✅ Easy scaling for each component independently  
✅ Native integration with Azure services  
✅ Well-documented, mature platform  

#### Cons
❌ Two separate services to deploy and manage  
❌ Need coordination between App Service and Functions  
❌ Higher cost than single-service options  
❌ Function cold starts (first execution delay)  
❌ Requires refactoring to extract scheduled task  
❌ Network latency between services if communicating  

#### Cost Estimate
- **App Service**: $13-55/month (Basic B1 - Standard S1)
- **Function App**: $0.20-10/month (Consumption plan, ~43,200 executions/month)
- **Storage**: $1-5/month (for Function state)
- **Total**: ~$15-70/month (depending on tier)

#### Complexity
**Level**: Moderate  
**Skills Required**: Basic Azure, Java deployment, Functions development  
**Setup Time**: 4-6 hours  

#### Best Use Case
- Traditional Java teams comfortable with App Service
- Need to scale API and scheduled task independently
- Willing to manage two services
- Budget allows for separate services

---

### Option 2: Azure Container Apps ⭐ **RECOMMENDED**

#### Architecture
- **Single Container**: Spring Boot app with both REST API and scheduled task
- **Deployment**: Docker container deployed to Azure Container Apps
- **Scheduled Task**: Runs in background thread (existing @Scheduled annotation)

#### Implementation Details

**Dockerfile**:
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/message-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Container Apps Configuration**:
- Runtime: Custom container (Docker)
- CPU/Memory: 0.5 vCPU, 1 GB RAM (adjustable)
- Scaling: 1-10 replicas (or scale to zero)
- Ingress: HTTP/HTTPS on port 8080

**Scheduled Task**:
No changes needed! The existing `@Scheduled(fixedDelay = 60000)` annotation works as-is. Each container replica runs the scheduled task.

**Note on Replicas**: If scaling > 1 replica, scheduled task runs in each replica. For exactly-once execution, consider:
- Option A: Keep replicas = 1 for scheduled tasks
- Option B: Use distributed locking (Redis, database)
- Option C: Split into separate Container App for scheduled task

#### Pros
✅ Modern, cloud-native deployment model  
✅ Single containerized application (no code refactoring)  
✅ Cost-effective with scale-to-zero capability  
✅ No need to split scheduled task from API  
✅ Docker provides consistent local/cloud experience  
✅ Built-in load balancing and auto-scaling  
✅ Supports microservices evolution  
✅ Lower cost than App Service  

#### Cons
❌ Requires Docker knowledge and container building  
❌ Initial setup complexity (Dockerfile, registry)  
❌ Container registry needed (Azure Container Registry)  
❌ Different operational model than traditional deployment  
❌ Need to manage replica count for scheduled tasks  

#### Cost Estimate
- **Container Apps**: $5-15/month (1 replica, 0.5 vCPU, 1 GB)
- **Container Registry**: $5/month (Basic tier)
- **Storage**: $1-2/month (container storage)
- **Total**: ~$11-22/month

**Scale to Zero**: If API is not constantly used, can scale to 0 and pay only for execution time.

#### Complexity
**Level**: Moderate  
**Skills Required**: Docker basics, container concepts, Azure Container Apps  
**Setup Time**: 3-5 hours (including Docker learning)  

#### Best Use Case
- ⭐ **Workshop/Learning Environment** (this scenario)
- Teams adopting cloud-native practices
- Cost-conscious projects
- Applications that can scale to zero during off-hours
- Future microservices architecture plans

---

### Option 3: Azure Spring Apps

#### Architecture
- **Deployment**: Spring Boot JAR uploaded directly to Azure Spring Apps
- **Runtime**: VMware Tanzu-based Spring optimized platform
- **Scheduled Task**: Runs as part of the application (no changes needed)

#### Implementation Details

**Configuration**:
- Runtime: Java 17
- Spring Boot: 3.x (native support)
- Deployment: JAR file (no Docker required)
- Service Binding: Can bind to Azure services (SQL, Redis, etc.)

**Deployment**:
```bash
az spring app create --name message-service --service my-spring-service
az spring app deploy --name message-service --artifact-path target/message-service.jar
```

No application code changes needed - deploy as-is!

#### Pros
✅ Purpose-built for Spring Boot applications  
✅ Zero application code changes (no Docker needed)  
✅ Advanced Spring Boot optimizations  
✅ VMware Tanzu integration (enterprise features)  
✅ Built-in monitoring with Application Insights  
✅ Blue-green deployments out of the box  
✅ Service registry and config server included  
✅ Simplified Spring configuration  

#### Cons
❌ **Highest cost** of all options  
❌ Overkill for simple applications  
❌ Vendor-specific platform (Azure + VMware)  
❌ Enterprise pricing model  
❌ Learning curve for platform features  
❌ Not available in all Azure regions  

#### Cost Estimate
- **Basic Tier**: $45-100/month (1 instance, 1 vCPU, 2 GB)
- **Standard Tier**: $180-300/month (enterprise features)
- **Total**: ~$45-300/month

Note: No separate container registry or storage costs.

#### Complexity
**Level**: Low-Moderate  
**Skills Required**: Spring Boot knowledge, Azure CLI  
**Setup Time**: 2-3 hours  

#### Best Use Case
- Enterprise Spring Boot applications
- Need advanced observability and APM
- Budget allows for premium service
- Want zero-code deployment changes
- Require blue-green deployments, service mesh

---

### Option 4: Azure Kubernetes Service (AKS)

#### Architecture
- **Container Orchestration**: Kubernetes cluster managing containers
- **Deployment**: Spring Boot app as Kubernetes Deployment
- **Scheduled Task**: CronJob or in-app @Scheduled

#### Implementation Details

**Kubernetes Resources**:
- Deployment: Spring Boot container (API)
- Service: LoadBalancer or Ingress for external access
- CronJob: Optional separate pod for scheduled task

**Sample Deployment**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: message-service
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: app
        image: myregistry.azurecr.io/message-service:latest
        ports:
        - containerPort: 8080
```

**Scheduled Task Options**:
1. Keep in application (same as Container Apps)
2. Separate CronJob: `schedule: "*/1 * * * *"`

#### Pros
✅ Full Kubernetes power and flexibility  
✅ Industry-standard orchestration  
✅ Advanced networking, security, scaling  
✅ Multi-container pod support  
✅ Ideal for complex microservices  
✅ Portable across cloud providers  

#### Cons
❌ **Highest complexity** of all options  
❌ Kubernetes expertise required  
❌ Overkill for simple applications  
❌ Operational overhead (cluster management)  
❌ Higher cost for small workloads  
❌ Steeper learning curve  

#### Cost Estimate
- **AKS Cluster**: $75-200/month (2-3 nodes)
- **Load Balancer**: $20-30/month
- **Container Registry**: $5/month
- **Total**: ~$100-235/month

#### Complexity
**Level**: High  
**Skills Required**: Kubernetes, Docker, YAML manifests, kubectl  
**Setup Time**: 8-12 hours  

#### Best Use Case
- Multiple microservices (5+)
- Need advanced Kubernetes features
- Team has Kubernetes expertise
- Require multi-cloud portability
- Complex networking requirements

---

### Option 5: All-in Azure Functions

#### Architecture
- **REST API**: HTTP-triggered Functions (one per endpoint)
- **Scheduled Task**: Timer-triggered Function
- **Execution**: Serverless, stateless functions

#### Implementation Details

**Challenges**:
- Need to refactor Spring Boot app into individual functions
- Each REST endpoint becomes a separate function
- Shared code extracted to common utilities
- Database access in each function

**Sample API Function**:
```java
@FunctionName("GetMessages")
public HttpResponseMessage run(
    @HttpTrigger(name = "req", methods = {HttpMethod.GET}, route = "messages") 
    HttpRequestMessage<Optional<String>> request,
    ExecutionContext context) {
    
    // Query database
    // Return JSON response
}
```

#### Pros
✅ Fully serverless (extreme scalability)  
✅ Pay-per-execution pricing  
✅ Zero server management  
✅ Fast auto-scaling  

#### Cons
❌ **Requires complete application refactoring**  
❌ Spring Boot framework cannot be used  
❌ Stateless execution model  
❌ Cold start latency  
❌ Function execution time limits (5-10 minutes)  
❌ Loss of Spring Boot benefits (DI, auto-config)  
❌ More complex development model  

#### Cost Estimate
- **Consumption Plan**: $5-15/month (low traffic)
- **Total**: ~$5-15/month

#### Complexity
**Level**: Very High (requires rewrite)  
**Skills Required**: Azure Functions, serverless patterns, stateless design  
**Setup Time**: 20-40 hours (complete refactor)  

#### Best Use Case
- Greenfield serverless-first projects
- Event-driven architectures
- Highly variable load (0 to 100,000 requests)
- Budget extremely constrained

**Not recommended for this migration** due to refactoring required.

---

### Deployment Options Comparison Matrix

| Criteria | App Service + Functions | Container Apps ⭐ | Spring Apps | AKS | All Functions |
|----------|------------------------|------------------|-------------|-----|---------------|
| **Monthly Cost** | $15-70 | $11-22 | $45-300 | $100-235 | $5-15 |
| **Complexity** | Moderate | Moderate | Low-Moderate | High | Very High |
| **Setup Time** | 4-6 hours | 3-5 hours | 2-3 hours | 8-12 hours | 20-40 hours |
| **Services Count** | 2 | 1 | 1 | 1 cluster | Multiple |
| **Code Changes** | Split task | None | None | None | Complete rewrite |
| **Docker Required** | No | Yes | No | Yes | No |
| **Scaling** | Independent | Auto | Auto | Advanced | Extreme |
| **Scheduled Task** | Functions Timer | @Scheduled | @Scheduled | CronJob/@Scheduled | Functions Timer |
| **Best For** | Traditional Java | Cloud-native | Enterprise Spring | Microservices | Serverless-first |

---

## 3. Recommended Approach

### Selected Option: Azure Container Apps ⭐

#### Justification

For this migration workshop, **Azure Container Apps** is the best choice because:

1. **Cost-Effective**: $11-22/month vs $45+ for Spring Apps or $100+ for AKS
2. **Modern Skills**: Docker and containers are essential cloud-native skills
3. **Simplicity**: Single service, no code refactoring needed
4. **Learning Value**: Great introduction to containerization
5. **No Code Split**: Scheduled task stays in the application
6. **Scalability**: Can grow from single container to microservices
7. **Industry Relevant**: Container Apps is growing in enterprise adoption

#### Why Not Other Options?

**App Service + Functions**: 
- ❌ Two services to manage
- ❌ Need to extract scheduled task
- ❌ Higher cost
- ✅ Consider if team resists Docker

**Spring Apps**:
- ❌ $45-300/month too expensive for workshop
- ❌ Enterprise features not needed for learning
- ✅ Consider for production enterprise apps

**AKS**:
- ❌ Too complex for single app
- ❌ Kubernetes overhead not justified
- ✅ Consider when you have 5+ microservices

**All Functions**:
- ❌ Requires complete rewrite
- ❌ Loss of Spring Boot benefits
- ✅ Only for greenfield serverless projects

### Migration Strategy

#### Phase 1: Code Modernization (Day 1-2)

**Tasks**:
1. Update pom.xml (Spring Boot 2.7 → 3.x parent)
2. Update JDK version (1.8 → 17)
3. Replace javax.* → jakarta.* imports
4. Migrate java.util.Date → java.time.LocalDateTime
5. Replace Log4j → SLF4J
6. Update @Controller → @RestController
7. Field injection → Constructor injection
8. Remove deprecated API usage (finalize, new Integer())
9. Update Commons Lang 2.x → 3.x
10. Fix Hibernate @Type annotation

**Deliverable**: Modernized Spring Boot 3.x application running locally

#### Phase 2: Containerization (Day 2)

**Tasks**:
1. Create Dockerfile
2. Build Docker image locally
3. Test container locally
4. Verify scheduled task runs every minute
5. Verify all API endpoints work

**Deliverable**: Docker image that runs successfully locally

#### Phase 3: Azure Deployment (Day 3)

**Tasks**:
1. Create Azure Container Registry
2. Push Docker image to registry
3. Create Azure Container Apps environment
4. Deploy container to Azure
5. Configure ingress (HTTP/HTTPS)
6. Test API endpoints in Azure
7. Verify scheduled task logs
8. Set up monitoring

**Deliverable**: Application running in Azure Container Apps

### Critical Success Factors

1. **Scheduled Task Timing**: Must run every 60 seconds
   - Verify with logs: Check execution timestamps
   - Monitor for missed executions
   - Alert if task fails

2. **Database Compatibility**: H2 in-memory works in container
   - Data resets on restart (expected for in-memory)
   - Consider persistent storage if needed later

3. **Logging**: Ensure logs are accessible in Azure
   - Use Azure Container Apps log streaming
   - Configure log retention
   - Set up alerts for errors

### Migration Timeline

| Phase | Duration | Key Milestones |
|-------|----------|----------------|
| **Phase 1**: Code Modernization | 12-16 hours | Spring Boot 3.x builds and runs locally |
| **Phase 2**: Containerization | 3-5 hours | Docker image runs locally |
| **Phase 3**: Azure Deployment | 4-6 hours | App runs in Azure with monitoring |
| **Buffer**: Testing & Fixes | 4-6 hours | Comprehensive testing, bug fixes |
| **Total** | **23-33 hours** | **~3-4 days** |

### Effort by Role

**Developer** (20-28 hours):
- Code changes
- Testing
- Dockerfile creation
- Local validation

**DevOps** (3-5 hours):
- Azure resource setup
- Container registry
- Deployment pipeline
- Monitoring configuration

---

## 4. Code Change Examples

### 4.1 Dependency Changes (pom.xml)

#### Before: Spring Boot 2.7.18
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
    <relativePath/>
</parent>

<properties>
    <java.version>1.8</java.version>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    <commons-lang.version>2.6</commons-lang.version>
    <log4j.version>1.2.17</log4j.version>
</properties>

<dependencies>
    <dependency>
        <groupId>commons-lang</groupId>
        <artifactId>commons-lang</artifactId>
        <version>${commons-lang.version}</version>
    </dependency>
    
    <dependency>
        <groupId>log4j</groupId>
        <artifactId>log4j</artifactId>
        <version>${log4j.version}</version>
    </dependency>
</dependencies>
```

#### After: Spring Boot 3.3.x
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.5</version> <!-- Latest stable -->
    <relativePath/>
</parent>

<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>

<dependencies>
    <!-- Commons Lang 3.x - spring-boot-starter-parent manages version -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>
    
    <!-- Log4j removed - Spring Boot 3.x uses SLF4J + Logback by default -->
</dependencies>
```

**Key Changes**:
- Spring Boot version: 2.7.18 → 3.3.5
- Java version: 1.8 → 17
- Remove Log4j dependency (SLF4J included in starters)
- commons-lang → commons-lang3

---

### 4.2 Package Imports (javax → jakarta)

#### Before: javax.* packages
```java
// Message.java
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

// MessageController.java
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

// MessageService.java
import javax.transaction.Transactional;
```

#### After: jakarta.* packages
```java
// Message.java
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// MessageController.java
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

// MessageService.java
import jakarta.transaction.Transactional;
```

**Migration Tool**: IntelliJ IDEA and VS Code can do this automatically with Find & Replace:
- Find: `import javax.(persistence|validation|servlet|transaction)`
- Replace: `import jakarta.$1`
- Use regex mode

---

### 4.3 Date API Usage (Date → LocalDateTime)

#### Before: java.util.Date
```java
// Message.java
import java.util.Date;

@Entity
public class Message {
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;
    
    @PrePersist
    protected void onCreate() {
        createdDate = new Date();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedDate = new Date();
    }
    
    // Getters/Setters for Date
    public Date getCreatedDate() {
        return createdDate;
    }
}
```

#### After: java.time.LocalDateTime
```java
// Message.java
import java.time.LocalDateTime;

@Entity
public class Message {
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
    
    // Getters/Setters for LocalDateTime
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
}
```

**Key Changes**:
- Remove `@Temporal` annotation (not needed for LocalDateTime)
- `new Date()` → `LocalDateTime.now()`
- Hibernate 6 natively supports java.time types

**Date Arithmetic** (Service Layer):
```java
// Before
Calendar calendar = Calendar.getInstance();
calendar.add(Calendar.DAY_OF_MONTH, -7);
Date cutoffDate = calendar.getTime();

// After
LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
```

**Date Formatting** (Controller):
```java
// Before
SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
String formatted = dateFormat.format(new Date());

// After
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
String formatted = LocalDateTime.now().format(formatter);
```

---

### 4.4 Controller Patterns

#### Before: @Controller + @ResponseBody
```java
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/api/messages")
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllMessages() {
        // ...
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMessageById(@PathVariable Long id) {
        // ...
    }
    
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createMessage(@RequestBody CreateMessageRequest request) {
        // ...
    }
}
```

#### After: @RestController + Specific Mappings
```java
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    
    private final MessageService messageService;
    
    // Constructor injection (Spring 4.3+ - @Autowired optional)
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMessages() {
        // ...
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMessageById(@PathVariable Long id) {
        // ...
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMessage(@RequestBody CreateMessageRequest request) {
        // ...
    }
}
```

**Key Changes**:
1. `@Controller` + `@ResponseBody` → `@RestController`
2. `@RequestMapping(method = RequestMethod.GET)` → `@GetMapping`
3. `@RequestMapping(method = RequestMethod.POST)` → `@PostMapping`
4. `@RequestMapping(method = RequestMethod.PUT)` → `@PutMapping`
5. `@RequestMapping(method = RequestMethod.DELETE)` → `@DeleteMapping`
6. `@Autowired` field → Constructor injection
7. `value = "/{id}"` remains in path-specific mappings

---

### 4.5 Logging (Log4j → SLF4J)

#### Before: Log4j 1.x
```java
import org.apache.log4j.Logger;

public class MessageController {
    
    private static final Logger logger = Logger.getLogger(MessageController.class);
    
    public void someMethod() {
        logger.info("Processing request");
        logger.error("Error occurred", exception);
        logger.debug("Debug information");
    }
}
```

#### After: SLF4J
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageController {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    
    public void someMethod() {
        logger.info("Processing request");
        logger.error("Error occurred", exception);
        logger.debug("Debug information");
    }
}
```

**Additional Change**: Remove log4j.properties

Spring Boot 3.x uses Logback by default with `application.properties`:
```properties
# application.properties
logging.level.com.nytour.demo=INFO
logging.level.org.springframework=WARN
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

---

### 4.6 Dependency Injection (Field → Constructor)

#### Before: Field Injection
```java
@Service
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }
}
```

#### After: Constructor Injection
```java
@Service
public class MessageService {
    
    private final MessageRepository messageRepository;
    
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }
    
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }
}
```

**Benefits**:
- Immutable dependencies (`final`)
- Easier to test (can provide mock in constructor)
- Clearer dependencies (visible in constructor signature)
- Null-safety (dependencies must be provided)
- No need for `@Autowired` annotation

**Lombok Alternative** (optional):
```java
@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepository messageRepository;
    
    // Constructor auto-generated by Lombok
    
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }
}
```

---

### 4.7 Remove Deprecated APIs

#### Primitive Wrapper Constructors

**Before**:
```java
Integer statusCode = new Integer(200);
Boolean isActive = new Boolean(true);
Long count = new Long(42);
```

**After**:
```java
Integer statusCode = 200; // Auto-boxing
Boolean isActive = true;   // Auto-boxing
Long count = 42L;          // Auto-boxing

// Or explicitly
Integer statusCode = Integer.valueOf(200);
Boolean isActive = Boolean.TRUE;
Long count = Long.valueOf(42);
```

#### finalize() Method

**Before**:
```java
@Override
protected void finalize() throws Throwable {
    try {
        logger.info("MessageScheduledTask is being garbage collected");
    } finally {
        super.finalize();
    }
}
```

**After**:
```java
// Remove finalize() method entirely
// If cleanup is needed, use @PreDestroy
@PreDestroy
public void cleanup() {
    logger.info("MessageScheduledTask is being destroyed");
}
```

---

### 4.8 Hibernate Type Annotation

#### Before: Hibernate 5.x
```java
import org.hibernate.annotations.Type;

@Entity
public class Message {
    
    @Type(type = "yes_no")
    @Column(name = "is_active")
    private Boolean active;
}
```

#### After: Hibernate 6.x
```java
// Option 1: Remove @Type (Hibernate 6 handles boolean well)
@Column(name = "is_active")
private Boolean active;

// Option 2: If yes/no char storage is required
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;

@Convert(converter = YesNoConverter.class)
@Column(name = "is_active")
private Boolean active;
```

---

### 4.9 Repository Date Parameter

#### Before: java.util.Date
```java
import java.util.Date;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE m.createdDate > :date AND m.active = true")
    List<Message> findRecentActiveMessages(@Param("date") Date date);
    
    List<Message> findByCreatedDateBetween(Date startDate, Date endDate);
}
```

#### After: java.time.LocalDateTime
```java
import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE m.createdDate > :date AND m.active = true")
    List<Message> findRecentActiveMessages(@Param("date") LocalDateTime date);
    
    List<Message> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
```

---

### 4.10 Dockerfile

**New file**: Create `Dockerfile` in project root

```dockerfile
# Use Eclipse Temurin (formerly AdoptOpenJDK) - official OpenJDK distribution
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy JAR file from target directory
COPY target/message-service.jar app.jar

# Expose port 8080
EXPOSE 8080

# Set Java options (optional)
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build and Run**:
```bash
# Build Spring Boot JAR
mvn clean package

# Build Docker image
docker build -t message-service:latest .

# Run container locally
docker run -p 8080:8080 message-service:latest

# Test
curl http://localhost:8080/api/messages
```

---

## 5. Effort Estimation

### Time Breakdown by Activity

| Activity | Estimated Time | Details |
|----------|----------------|---------|
| **pom.xml updates** | 1-2 hours | Parent version, dependencies, Java version |
| **Package imports (javax → jakarta)** | 2-3 hours | Global find/replace, compilation check |
| **Date API migration** | 4-6 hours | Entity, Service, Controller, Repository |
| **Controller modernization** | 2-3 hours | @RestController, @GetMapping, etc. |
| **Constructor injection** | 2-3 hours | Controller, Service, Task classes |
| **Logging (Log4j → SLF4J)** | 1-2 hours | Import changes, test logging |
| **Deprecated API removal** | 1-2 hours | finalize(), wrapper constructors |
| **Hibernate type fixes** | 1 hour | @Type annotation updates |
| **Commons Lang 3.x** | 0.5 hours | Import change |
| **Local testing** | 2-4 hours | Build, run, test all endpoints |
| **Dockerfile creation** | 1-2 hours | Write Dockerfile, build image |
| **Local container testing** | 1-2 hours | Run container, verify functionality |
| **Azure setup** | 2-3 hours | Container Registry, Container Apps env |
| **Azure deployment** | 2-3 hours | Push image, deploy, configure |
| **Azure testing** | 1-2 hours | Verify endpoints, scheduled task, logs |
| **Documentation** | 2-3 hours | Update README, deployment guide |
| **Contingency (15%)** | 4-6 hours | Bug fixes, unexpected issues |
| **TOTAL** | **29-45 hours** | **4-6 days** |

### Detailed Breakdown by Component

This section provides a granular breakdown of effort for each file and component in the codebase.

#### Configuration Files (3-4 hours)

| File | Estimated Time | Changes Required |
|------|----------------|------------------|
| **pom.xml** | 1-2 hours | Spring Boot parent 2.7.18→3.3.5, Java 1.8→17, remove Log4j, add Commons Lang 3, update H2 version |
| **application.properties** | 0.5 hours | Update logging properties (if needed), verify H2 settings |
| **log4j.properties** | 0.5 hours | Remove file (migrate to Logback via application.properties) |
| **Dockerfile** (new) | 1-2 hours | Create new file for containerization |

#### Java Files - Code Modernization (12-16 hours)

| File | Lines | Estimated Time | Changes Required |
|------|-------|----------------|------------------|
| **Application.java** | 25 | 0.5 hours | Minimal changes - verify Spring Boot 3.x compatibility, test startup |
| **Message.java** | 138 | 2-3 hours | • javax→jakarta imports (persistence, validation)<br>• Date→LocalDateTime (createdDate, updatedDate)<br>• Remove @Temporal annotations<br>• Fix @Type annotation for Hibernate 6<br>• Remove deprecated Boolean constructor<br>• Update @PrePersist and @PreUpdate<br>• Test entity persistence |
| **MessageRepository.java** | 44 | 1-2 hours | • javax→jakarta imports<br>• Update Date parameters→LocalDateTime<br>• Verify query methods<br>• Test all repository operations |
| **MessageService.java** | 101 | 2-3 hours | • Change Date/Calendar→LocalDateTime<br>• Update Commons Lang 2.x→3.x imports<br>• Field injection→Constructor injection<br>• Update date arithmetic logic<br>• Test all service methods |
| **MessageController.java** | 276 | 3-4 hours | • javax→jakarta imports (servlet, validation)<br>• @Controller+@ResponseBody→@RestController<br>• @RequestMapping(method=GET)→@GetMapping<br>• SimpleDateFormat→DateTimeFormatter<br>• Log4j→SLF4J imports<br>• Field injection→Constructor injection<br>• Date→LocalDateTime in responses<br>• Test all REST endpoints |
| **MessageScheduledTask.java** | 125 | 2-3 hours | • Log4j→SLF4J imports<br>• Date/Calendar→LocalDateTime<br>• SimpleDateFormat→DateTimeFormatter<br>• Remove finalize() method<br>• Remove deprecated Integer constructor<br>• Field injection→Constructor injection<br>• Verify 60-second schedule works<br>• Test scheduled execution |

#### Summary by File Type

| Component | Files | Total Lines | Estimated Time |
|-----------|-------|-------------|----------------|
| Configuration | 3 (+ 1 new) | N/A | 3-4 hours |
| Java Classes | 6 | ~709 lines | 12-16 hours |
| Testing & Validation | N/A | N/A | 6-8 hours |
| Container & Azure | N/A | N/A | 8-12 hours |
| **Total** | **9-10 files** | **~709 Java lines** | **29-40 hours** |

#### Clarification on Scope

**Actual Repository Size**:
- Java files: 6 (not 50+)
- Configuration files: 3 (pom.xml, application.properties, log4j.properties)
- Total files to modify: 9 files

**Note**: If your repository has 50+ Java classes not visible in the main branch, the effort estimate would need to be scaled accordingly. For each additional Java class with similar complexity:
- Simple class (entity/DTO): +0.5-1 hour
- Service/Controller class: +1-2 hours
- Complex class with legacy patterns: +2-3 hours

For a 50-class codebase, total effort would be approximately **80-120 hours (2-3 weeks)**.

### Estimation by Team Experience

| Team Profile | Estimated Duration | Notes |
|--------------|-------------------|-------|
| **Experienced Java/Spring** | 3-4 days | Familiar with Spring Boot 3.x migration |
| **Mid-level Java/Spring** | 4-5 days | Learning Spring Boot 3.x concepts |
| **New to Docker** | 5-6 days | Additional time for containerization |
| **New to Azure** | 5-6 days | Additional time for Azure learning |
| **Full stack (Java + Docker + Azure)** | 3 days | Optimal conditions |

### Milestones

| Milestone | Target | Completion Criteria |
|-----------|--------|---------------------|
| **Code Modernized** | End of Day 2 | Spring Boot 3.x app builds, runs locally |
| **Containerized** | End of Day 2 | Docker image runs locally, all features work |
| **Deployed to Azure** | End of Day 3 | App running in Azure Container Apps |
| **Production Ready** | End of Day 4 | Monitoring, logging, testing complete |

---

## 6. Risk Assessment

### High-Risk Areas 🔴

#### Risk 1: Scheduled Task Timing

**Description**: Scheduled task MUST run every 60 seconds (business critical)

**Likelihood**: Medium  
**Impact**: High  
**Risk Level**: 🔴 HIGH

**Potential Issues**:
- Container restarts reset scheduling
- Multiple replicas = multiple executions
- Azure platform delays

**Mitigation**:
1. **Test Early**: Verify @Scheduled works in container locally
2. **Monitor**: Set up Azure Monitor alerts for missed executions
3. **Logging**: Log every execution with timestamp
4. **Verify**: Check logs in Azure to confirm 60-second interval
5. **Replica Strategy**: Keep replicas = 1 OR implement distributed lock

**Validation Criteria**:
```
✅ Task executes exactly once per minute
✅ No missed executions over 24-hour period
✅ Execution times are consistent (60±2 seconds)
✅ Logs show all executions
```

**Alternative**: If Container Apps scheduling is unreliable, fall back to Azure Functions Timer Trigger.

---

#### Risk 2: Database Schema Changes (Hibernate 5 → 6)

**Description**: Hibernate 6 may generate different schema or have different types

**Likelihood**: Low-Medium  
**Impact**: High  
**Risk Level**: 🔴 HIGH

**Potential Issues**:
- Column type changes (boolean storage)
- Sequence generation changes
- JPQL query syntax differences

**Mitigation**:
1. **H2 Compatibility**: Use compatible H2 version (2.x with Spring Boot 3.x)
2. **Test Data**: Create comprehensive test data before migration
3. **Compare Schema**: Export schema before/after, compare differences
4. **Integration Tests**: Write tests for all queries
5. **Manual Testing**: Test CRUD operations thoroughly

**Validation Criteria**:
```
✅ All entities persist correctly
✅ All queries return expected results
✅ Boolean fields store correctly (is_active)
✅ Date fields store as TIMESTAMP
✅ No data loss on update/delete
```

---

#### Risk 3: Package Namespace Changes (javax → jakarta)

**Description**: All javax.* must become jakarta.* - easy to miss some

**Likelihood**: Medium  
**Impact**: High (compilation failure)  
**Risk Level**: 🔴 HIGH

**Potential Issues**:
- Missed imports in inner classes
- External libraries still using javax.*
- DTOs nested in controllers

**Mitigation**:
1. **Automated Find/Replace**: Use IDE regex replace
2. **Compilation Check**: `mvn clean compile` after every change
3. **Systematic Approach**: Process one package at a time
4. **Code Review**: Check all Java files for remaining javax.*

**Affected Packages**:
```
javax.persistence.*       → jakarta.persistence.*
javax.validation.*        → jakarta.validation.*
javax.servlet.*           → jakarta.servlet.*
javax.transaction.*       → jakarta.transaction.*
```

**Validation Criteria**:
```
✅ mvn clean compile succeeds
✅ No javax.* imports remain (except javax.crypto, etc. - JDK packages)
✅ Application starts without errors
✅ All endpoints respond correctly
```

---

### Medium-Risk Areas 🟡

#### Risk 4: Date/Time API Migration

**Description**: Date → LocalDateTime affects JSON serialization, database, comparisons

**Likelihood**: Medium  
**Impact**: Medium  
**Risk Level**: 🟡 MEDIUM

**Potential Issues**:
- JSON format changes (ISO-8601 vs custom format)
- Database column type changes
- Date arithmetic errors
- Timezone handling differences

**Mitigation**:
1. **Consistent Approach**: Use LocalDateTime everywhere (not mix with Date)
2. **JSON Testing**: Verify API responses have correct date format
3. **Database Testing**: Verify dates persist and query correctly
4. **Repository Updates**: Update all Date parameters to LocalDateTime

**Validation Criteria**:
```
✅ JSON responses show dates in expected format
✅ Database stores dates correctly
✅ Date queries return expected results
✅ Date arithmetic works (e.g., minusDays(7))
```

---

#### Risk 5: Logging Framework Transition

**Description**: Log4j 1.x → SLF4J/Logback may have different behavior

**Likelihood**: Low  
**Impact**: Medium  
**Risk Level**: 🟡 MEDIUM

**Potential Issues**:
- Missing log output
- Different log format
- Log level configuration not working
- Performance characteristics

**Mitigation**:
1. **Keep API Same**: SLF4J API is almost identical to Log4j
2. **Configure Logback**: Use application.properties for log levels
3. **Test Logging**: Verify logs appear at all levels
4. **Pattern Match**: Keep similar log format to old logs

**Validation Criteria**:
```
✅ Log statements appear in console/file
✅ Log levels work (DEBUG, INFO, WARN, ERROR)
✅ Scheduled task logs appear every minute
✅ Exception stack traces are complete
```

---

#### Risk 6: Docker Container Differences

**Description**: Container environment differs from local development

**Likelihood**: Medium  
**Impact**: Medium  
**Risk Level**: 🟡 MEDIUM

**Potential Issues**:
- File path differences (Windows vs Linux)
- Environment variables not set
- Network connectivity issues
- Resource limits (CPU, memory)

**Mitigation**:
1. **Test Locally First**: Run Docker image locally before Azure
2. **Environment Parity**: Use same JDK version local and container
3. **Externalize Config**: Use environment variables for config
4. **Health Checks**: Implement /actuator/health endpoint

**Validation Criteria**:
```
✅ Container starts successfully
✅ Application logs show startup
✅ Health endpoint responds
✅ API endpoints work
✅ Scheduled task executes
```

---

### Low-Risk Areas 🟢

#### Risk 7: Maven Build Changes

**Description**: Maven configuration updates for Java 17

**Likelihood**: Low  
**Impact**: Low  
**Risk Level**: 🟢 LOW

**Mitigation**: Follow Spring Boot 3.x parent BOM (dependency versions managed)

---

#### Risk 8: H2 Database Version

**Description**: H2 version compatibility with Hibernate 6

**Likelihood**: Low  
**Impact**: Low  
**Risk Level**: 🟢 LOW

**Mitigation**: Spring Boot 3.x parent manages H2 version automatically

---

#### Risk 9: Azure Container Apps Limits

**Description**: Platform limits (request size, timeout, etc.)

**Likelihood**: Low  
**Impact**: Low  
**Risk Level**: 🟢 LOW

**Mitigation**: This app is well within limits (simple REST API)

---

### Risk Mitigation Strategy

#### Pre-Migration
- [ ] Back up current codebase (Git tag)
- [ ] Document current behavior (API responses, logs)
- [ ] Create test plan for validation
- [ ] Set up rollback procedure

#### During Migration
- [ ] Make changes incrementally
- [ ] Test after each major change
- [ ] Commit frequently with clear messages
- [ ] Keep track of issues encountered

#### Post-Migration
- [ ] Run comprehensive test suite
- [ ] Monitor scheduled task for 24 hours
- [ ] Check logs for errors/warnings
- [ ] Performance comparison (before/after)
- [ ] Document any behavior changes

---

## Summary and Next Steps

### Assessment Summary

✅ **Migration is Feasible**: The application can be successfully migrated to Spring Boot 3.x and JDK 17 with moderate effort.

✅ **Recommended Deployment**: Azure Container Apps provides the best balance of cost, simplicity, and learning value.

✅ **Estimated Effort**: 3-4 days (29-45 hours) for experienced Spring Boot developers.

✅ **Critical Requirement Met**: Scheduled task will run every 60 seconds using existing @Scheduled annotation.

✅ **Risk Level**: Manageable - High-risk areas have clear mitigation strategies.

### Key Findings

| Aspect | Current | Target | Change Required |
|--------|---------|--------|-----------------|
| **JDK** | 1.8 | 17 | Update pom.xml, fix deprecated APIs |
| **Spring Boot** | 2.7.18 | 3.3.x | Update parent, change patterns |
| **Packages** | javax.* | jakarta.* | Global find/replace |
| **Logging** | Log4j 1.x | SLF4J | Import changes |
| **Date API** | java.util.Date | java.time | Refactor date handling |
| **Deployment** | JAR | Docker | Create Dockerfile |
| **Cloud** | None | Azure | Set up Azure resources |

### Next Steps

#### For Workshop Participants

1. **Review This Assessment**: Understand all sections
2. **Choose Deployment Option**: Confirm Azure Container Apps or alternative
3. **Create Migration Issue**: Request GitHub Copilot to implement
4. **Follow Migration**: Work through Step 3, 4, 5, 6 of workshop
5. **Deploy to Azure**: Complete cloud deployment

#### Implementation Order

**Phase 1: Dependencies**
1. Update pom.xml (Spring Boot parent, Java version)
2. Update/remove deprecated dependencies
3. Compile to identify issues

**Phase 2: Package Migration**
1. javax.* → jakarta.* (global replace)
2. Fix compilation errors
3. Verify all imports

**Phase 3: Code Modernization**
1. Date → LocalDateTime migration
2. Controller pattern updates
3. Dependency injection improvements
4. Logging framework change
5. Remove deprecated APIs

**Phase 4: Testing**
1. Local build and run
2. Test all API endpoints
3. Verify scheduled task
4. Integration testing

**Phase 5: Containerization**
1. Create Dockerfile
2. Build image
3. Test locally

**Phase 6: Azure Deployment**
1. Set up Azure resources
2. Push to Container Registry
3. Deploy to Container Apps
4. Configure and test

---

## Appendix

### A. Useful Commands

#### Maven
```bash
# Clean build
mvn clean package

# Run locally
mvn spring-boot:run

# Run tests
mvn test

# Check dependencies
mvn dependency:tree
```

#### Docker
```bash
# Build image
docker build -t message-service:latest .

# Run container
docker run -p 8080:8080 message-service:latest

# View logs
docker logs <container-id>

# Stop container
docker stop <container-id>
```

#### Azure CLI
```bash
# Login
az login

# Create resource group
az group create --name rg-message-service --location eastus

# Create Container Registry
az acr create --name myregistry --resource-group rg-message-service --sku Basic

# Create Container Apps environment
az containerapp env create --name my-environment --resource-group rg-message-service --location eastus

# Deploy
az containerapp create --name message-service --resource-group rg-message-service --environment my-environment --image myregistry.azurecr.io/message-service:latest --target-port 8080 --ingress external
```

### B. Testing Checklist

After migration, verify:

- [ ] Application starts without errors
- [ ] `GET /api/messages` returns message list
- [ ] `POST /api/messages` creates new message
- [ ] `GET /api/messages/{id}` returns specific message
- [ ] `PUT /api/messages/{id}` updates message
- [ ] `DELETE /api/messages/{id}` deletes message
- [ ] `GET /api/messages/search?keyword=test` searches messages
- [ ] `GET /api/messages/author/{author}` filters by author
- [ ] Scheduled task logs appear every 60 seconds
- [ ] Scheduled task reports correct statistics
- [ ] H2 console accessible (if enabled)
- [ ] Logs are visible and formatted correctly

### C. References

- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Spring Boot 3.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes)
- [Java 17 Migration Guide](https://docs.oracle.com/en/java/javase/17/migrate/getting-started.html)
- [Jakarta EE 9 Documentation](https://jakarta.ee/specifications/platform/9/)
- [Azure Container Apps Documentation](https://learn.microsoft.com/en-us/azure/container-apps/)
- [Hibernate 6 Migration Guide](https://hibernate.org/orm/releases/6.0/)

---

**End of Assessment**

*This assessment was created for the Java Migration Workshop. For questions or clarification, please comment on the related GitHub issue.*
