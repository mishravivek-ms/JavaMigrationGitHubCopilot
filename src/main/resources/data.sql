-- H2 Database initialization script
-- This will be executed automatically on application startup

-- Insert sample messages (using auto-generated IDs)
INSERT INTO messages (content, author, created_date, updated_date, is_active) 
VALUES ('Welcome to the Message Service!', 'admin', CURRENT_TIMESTAMP, NULL, TRUE);

INSERT INTO messages (content, author, created_date, updated_date, is_active) 
VALUES ('This application has been migrated to Spring Boot 3.x and JDK 17!', 'system', CURRENT_TIMESTAMP, NULL, TRUE);

INSERT INTO messages (content, author, created_date, updated_date, is_active) 
VALUES ('Now using modern jakarta.* packages and java.time APIs!', 'admin', CURRENT_TIMESTAMP, NULL, TRUE);

INSERT INTO messages (content, author, created_date, updated_date, is_active) 
VALUES ('Using H2 in-memory database for easy testing', 'system', CURRENT_TIMESTAMP, NULL, TRUE);

INSERT INTO messages (content, author, created_date, updated_date, is_active) 
VALUES ('Modern code with LocalDateTime, SLF4J, and constructor injection', 'developer', CURRENT_TIMESTAMP, NULL, TRUE);
