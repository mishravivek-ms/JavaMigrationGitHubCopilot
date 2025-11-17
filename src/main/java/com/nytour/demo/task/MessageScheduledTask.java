package com.nytour.demo.task;

import com.nytour.demo.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message Scheduled Task - Runs every minute
 * 
 * MIGRATION CHANGES:
 * 1. Constructor injection instead of field injection
 * 2. SLF4J logging instead of Log4j 1.x
 * 3. DateTimeFormatter instead of SimpleDateFormat
 * 4. LocalDateTime instead of Date and Calendar
 * 5. Removed deprecated finalize() method
 * 6. Removed deprecated Integer constructor
 */
@Component
public class MessageScheduledTask {

    // SLF4J logging (modern standard)
    private static final Logger logger = LoggerFactory.getLogger(MessageScheduledTask.class);

    // Constructor injection (modern Spring best practice)
    private final MessageService messageService;

    // DateTimeFormatter (thread-safe, modern replacement for SimpleDateFormat)
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MessageScheduledTask(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Scheduled task that runs every 60 seconds (every minute)
     * CRITICAL: This must run every minute as a business requirement
     */
    @Scheduled(fixedDelay = 60000) // 60000ms = 60 seconds = 1 minute
    public void reportMessageStatistics() {
        logger.info("========================================");
        logger.info("Message Statistics Task - Executing");
        logger.info("========================================");

        try {
            // Get current timestamp using modern LocalDateTime
            LocalDateTime now = LocalDateTime.now();
            logger.info("Execution Time: {}", now.format(dateFormatter));

            // Get message statistics
            Long totalMessages = (long) messageService.getAllMessages().size();
            Long activeMessages = messageService.getActiveMessageCount();

            logger.info("Total Messages: {}", totalMessages);
            logger.info("Active Messages: {}", activeMessages);
            logger.info("Inactive Messages: {}", (totalMessages - activeMessages));

            // Calculate messages from last 7 days using modern java.time API
            logger.info("Messages from last 7 days: {}", 
                messageService.getRecentMessages(7).size());

            // Log next execution time using modern API
            LocalDateTime nextExecution = now.plusMinutes(1);
            logger.info("Next Execution: {}", nextExecution.format(dateFormatter));

            // Modern status code (no deprecated constructor)
            Integer statusCode = Integer.valueOf(200);
            logger.info("Task Status Code: {}", statusCode);

            logger.info("Task completed successfully");

        } catch (Exception e) {
            logger.error("Error executing scheduled task", e);
            logger.error("Error message: {}", e.getMessage());
        }

        logger.info("========================================");
    }

    /**
     * Alternative scheduling method - using cron expression
     * Commented out but shows another scheduling approach
     */
    // @Scheduled(cron = "0 * * * * *") // Every minute at 0 seconds
    public void reportMessageStatisticsWithCron() {
        // Same logic as above, but triggered by cron expression
        reportMessageStatistics();
    }
}
