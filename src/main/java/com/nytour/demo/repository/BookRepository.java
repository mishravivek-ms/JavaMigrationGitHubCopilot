package com.nytour.demo.repository;

import com.nytour.demo.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Book Repository - Spring Data JPA 1.x (legacy)
 * Following the same pattern as MessageRepository
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Find by author (standard Spring Data JPA)
    List<Book> findByAuthor(String author);

    // Find by title containing (case-insensitive search)
    List<Book> findByTitleContainingIgnoreCase(String title);

    // Find available books (legacy boolean handling)
    List<Book> findByAvailableTrue();

    // Find by ISBN
    Book findByIsbn(String isbn);

    // Find by date range using deprecated Date API
    List<Book> findByCreatedDateBetween(Date startDate, Date endDate);

    // Custom JPQL query with Date parameter
    @Query("SELECT b FROM Book b WHERE b.createdDate > :date AND b.available = true")
    List<Book> findRecentAvailableBooks(@Param("date") Date date);

    // Count available books
    Long countByAvailableTrue();

    // Delete by author (Spring Data JPA 1.x style)
    void deleteByAuthor(String author);
}
