package com.nytour.demo.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Book Entity - Legacy JPA 2.1 with javax.persistence
 * Following the same pattern as Message entity
 */
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "Title cannot be null")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @NotNull(message = "Author cannot be null")
    @Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters")
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Size(max = 50, message = "ISBN must not exceed 50 characters")
    @Column(name = "isbn", length = 50)
    private String isbn;

    @Column(name = "price")
    private Double price;

    // Using deprecated java.util.Date instead of java.time.LocalDateTime
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_date")
    private Date updatedDate;

    @Column(name = "is_available")
    @Type(type = "yes_no") // Hibernate 4.x specific type annotation
    private Boolean available;

    // Default constructor required by JPA
    public Book() {
        // Initialize with deprecated Date constructor
        this.createdDate = new Date();
        this.available = Boolean.TRUE;
    }

    // Constructor with deprecated Date API
    public Book(String title, String author, String isbn, Double price) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.createdDate = new Date();
        this.available = new Boolean(true); // Deprecated constructor
    }

    // PrePersist callback using deprecated Date
    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = new Date();
        }
    }

    // PreUpdate callback using deprecated Date
    @PreUpdate
    protected void onUpdate() {
        updatedDate = new Date();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", price=" + price +
                ", createdDate=" + createdDate +
                ", available=" + available +
                '}';
    }
}
