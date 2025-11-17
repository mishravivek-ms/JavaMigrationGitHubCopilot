package com.nytour.demo.controller;

import com.nytour.demo.model.Message;
import com.nytour.demo.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message Controller - Modern Spring Boot 3.x patterns
 * 
 * MIGRATION CHANGES:
 * 1. @RestController instead of @Controller + @ResponseBody
 * 2. @GetMapping, @PostMapping, etc. instead of @RequestMapping(method=...)
 * 3. SLF4J logging instead of Log4j 1.x
 * 4. DateTimeFormatter instead of SimpleDateFormat
 * 5. Constructor injection instead of field injection
 * 6. LocalDateTime instead of Date
 * 7. jakarta.* packages instead of javax.*
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    // SLF4J logging (modern standard)
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    // Constructor injection (modern Spring best practice)
    private final MessageService messageService;

    // DateTimeFormatter (thread-safe, modern)
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Get all messages - Using @GetMapping
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMessages(HttpServletRequest request) {
        logger.info("GET /messages - Fetching all messages");
        
        try {
            List<Message> messages = messageService.getAllMessages();
            
            // Modern response building
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", messages);
            response.put("count", messages.size());
            response.put("timestamp", LocalDateTime.now().format(dateFormatter));
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching messages", e);
            return handleError("Failed to fetch messages", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get message by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMessageById(
            @PathVariable("id") Long id,
            HttpServletResponse response) {
        
        logger.info("GET /messages/{}", id);
        
        try {
            Message message = messageService.getMessageById(id);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", "success");
            responseMap.put("data", message);
            
            return new ResponseEntity<>(responseMap, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Message not found: {}", id, e);
            return handleError("Message not found with id: " + id, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Create new message - Using @Valid with jakarta.validation
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMessage(
            @Valid @RequestBody CreateMessageRequest request) {
        
        logger.info("POST /messages - Creating message from author: {}", request.getAuthor());
        
        try {
            Message message = messageService.createMessage(request.getContent(), request.getAuthor());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Message created successfully");
            response.put("data", message);
            response.put("createdAt", LocalDateTime.now().format(dateFormatter));
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid message data", e);
            return handleError(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update message
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMessage(
            @PathVariable("id") Long id,
            @RequestBody UpdateMessageRequest request) {
        
        logger.info("PUT /messages/{}", id);
        
        try {
            Message message = messageService.updateMessage(id, request.getContent());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Message updated successfully");
            response.put("data", message);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error updating message", e);
            return handleError("Failed to update message", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMessage(@PathVariable("id") Long id) {
        logger.info("DELETE /messages/{}", id);
        
        try {
            messageService.deleteMessage(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Message deleted successfully");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error deleting message", e);
            return handleError("Failed to delete message", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Search messages by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMessages(
            @RequestParam(value = "keyword", required = false) String keyword) {
        
        logger.info("GET /messages/search?keyword={}", keyword);
        
        List<Message> messages = messageService.searchMessages(keyword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", messages);
        response.put("count", messages.size());
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get messages by author
     */
    @GetMapping("/author/{author}")
    public ResponseEntity<Map<String, Object>> getMessagesByAuthor(@PathVariable("author") String author) {
        logger.info("GET /messages/author/{}", author);
        
        List<Message> messages = messageService.getMessagesByAuthor(author);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", messages);
        response.put("author", author);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        logger.info("GET /messages/stats");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessages", messageService.getAllMessages().size());
        stats.put("activeMessages", messageService.getActiveMessageCount());
        stats.put("timestamp", LocalDateTime.now().format(dateFormatter));
        
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    // Helper method for error responses
    private ResponseEntity<Map<String, Object>> handleError(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now().format(dateFormatter));
        
        return new ResponseEntity<>(errorResponse, status);
    }

    // Inner classes for request DTOs
    
    public static class CreateMessageRequest {
        @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.Size(min = 1, max = 500)
        private String content;
        
        @jakarta.validation.constraints.NotNull
        private String author;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }
    }

    public static class UpdateMessageRequest {
        @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.Size(min = 1, max = 500)
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
