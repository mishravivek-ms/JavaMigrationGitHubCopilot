package com.nytour.demo.repository;

import com.nytour.demo.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Message Repository - Spring Data JPA 3.x
 * 
 * MIGRATION CHANGES:
 * 1. Spring Data JPA 2.x to 3.x (no major API changes)
 * 2. Date parameters changed to LocalDateTime
 * 3. Query method naming conventions remain the same
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find by author (standard Spring Data JPA)
    List<Message> findByAuthor(String author);

    // Find active messages (legacy boolean handling)
    List<Message> findByActiveTrue();

    // Find by date range using modern LocalDateTime
    List<Message> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Custom JPQL query with LocalDateTime parameter
    @Query("SELECT m FROM Message m WHERE m.createdDate > :date AND m.active = true")
    List<Message> findRecentActiveMessages(@Param("date") LocalDateTime date);

    // Count active messages
    Long countByActiveTrue();

    // Find by content containing (case-insensitive search)
    List<Message> findByContentContainingIgnoreCase(String keyword);

    // Delete by author (Spring Data JPA 1.x style)
    void deleteByAuthor(String author);
}
