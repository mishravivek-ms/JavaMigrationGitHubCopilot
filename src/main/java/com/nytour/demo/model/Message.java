package com.nytour.demo.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Message Entity - Modern Spring Boot 3.x with Jakarta EE
 * 
 * MIGRATION CHANGES:
 * 1. javax.persistence.* → jakarta.persistence.*
 * 2. java.util.Date → java.time.LocalDateTime
 * 3. @Type annotation → @JdbcTypeCode for Hibernate 6.x
 * 4. Removed deprecated Boolean constructor
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Content cannot be null")
    @Size(min = 1, max = 500, message = "Content must be between 1 and 500 characters")
    @Column(nullable = false, length = 500)
    private String content;

    @NotNull
    @Column(name = "author")
    private String author;

    // Using modern java.time API instead of deprecated Date
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "is_active")
    @JdbcTypeCode(SqlTypes.BOOLEAN)
    private Boolean active;

    // Default constructor required by JPA
    public Message() {
        // Initialize with modern LocalDateTime
        this.createdDate = LocalDateTime.now();
        this.active = Boolean.TRUE;
    }

    // Constructor with modern java.time API
    public Message(String content, String author) {
        this.content = content;
        this.author = author;
        this.createdDate = LocalDateTime.now();
        this.active = Boolean.TRUE;
    }

    // PrePersist callback using modern LocalDateTime
    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    // PreUpdate callback using modern LocalDateTime
    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", author='" + author + '\'' +
                ", createdDate=" + createdDate +
                ", active=" + active +
                '}';
    }
}
