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
 */
@Component
public class MessageScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(MessageScheduledTask.class);

    private final MessageService messageService;

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MessageScheduledTask(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Scheduled task that runs every 60 seconds (every minute)
     */
    @Scheduled(fixedDelay = 60000)
    public void reportMessageStatistics() {
        logger.info("========================================");
        logger.info("Message Statistics Task - Executing");
        logger.info("========================================");

        try {
            LocalDateTime now = LocalDateTime.now();
            logger.info("Execution Time: " + dateFormat.format(now));

            Long totalMessages = Long.valueOf(messageService.getAllMessages().size());
            Long activeMessages = messageService.getActiveMessageCount();

            logger.info("Total Messages: " + totalMessages);
            logger.info("Active Messages: " + activeMessages);
            logger.info("Inactive Messages: " + (totalMessages - activeMessages));

            logger.info("Messages from last 7 days: " + 
                messageService.getRecentMessages(7).size());

            LocalDateTime nextExecution = now.plusMinutes(1);
            logger.info("Next Execution: " + dateFormat.format(nextExecution));

            Integer statusCode = 200;
            logger.info("Task Status Code: " + statusCode);

            logger.info("Task completed successfully");

        } catch (Exception e) {
            logger.error("Error executing scheduled task", e);
            logger.error("Error message: " + e.getMessage());
        }

        logger.info("========================================");
    }

    /**
     * Alternative scheduling method - using cron expression
     */
    // @Scheduled(cron = "0 * * * * *")
    public void reportMessageStatisticsWithCron() {
        reportMessageStatistics();
    }

    /**
     * Helper method using LocalDateTime
     */
    private String getTimeUntilNextRun() {
        LocalDateTime now = LocalDateTime.now();
        int secondsIntoMinute = now.getSecond();
        int secondsUntilNextMinute = 60 - secondsIntoMinute;
        
        return secondsUntilNextMinute + " seconds";
    }
}
