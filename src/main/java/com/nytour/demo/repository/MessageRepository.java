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
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByAuthor(String author);

    List<Message> findByActiveTrue();

    List<Message> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT m FROM Message m WHERE m.createdDate > :date AND m.active = true")
    List<Message> findRecentActiveMessages(@Param("date") LocalDateTime date);

    Long countByActiveTrue();

    List<Message> findByContentContainingIgnoreCase(String keyword);

    void deleteByAuthor(String author);
}
