package com.nytour.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Modern Spring Boot 3.x Application
 * 
 * Migration Completed:
 * - Spring Boot 3.3.x (uses jakarta.*, requires JDK 17+)
 * - JDK 17 LTS
 * - Modern Java APIs (java.time, etc.)
 * - SLF4J logging with Logback
 * - @SpringBootApplication replaces XML configuration
 * - @EnableScheduling activates @Scheduled tasks
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
