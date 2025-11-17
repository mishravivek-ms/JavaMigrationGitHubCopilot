package com.nytour.demo.service;

import com.nytour.demo.model.Book;
import com.nytour.demo.repository.BookRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Book Service - Legacy Spring 4.x patterns
 * Following the same pattern as MessageService
 */
@Service
@Transactional
public class BookService {

    // Field injection (legacy pattern, constructor injection preferred in modern Spring)
    @Autowired
    private BookRepository bookRepository;

    public Book createBook(String title, String author, String isbn, Double price) {
        // Using deprecated commons-lang StringUtils (version 2.x)
        if (StringUtils.isEmpty(title) || StringUtils.isEmpty(author)) {
            throw new IllegalArgumentException("Title and author cannot be empty");
        }

        Book book = new Book(title, author, isbn, price);
        return bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        // Legacy pattern: direct get without Optional handling
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            throw new RuntimeException("Book not found with id: " + id);
        }
        return book;
    }

    public Book updateBook(Long id, String title, String author, String isbn, Double price) {
        Book book = getBookById(id);
        
        if (!StringUtils.isEmpty(title)) {
            book.setTitle(title);
        }
        if (!StringUtils.isEmpty(author)) {
            book.setAuthor(author);
        }
        if (!StringUtils.isEmpty(isbn)) {
            book.setIsbn(isbn);
        }
        if (price != null) {
            book.setPrice(price);
        }
        
        // Using deprecated Date and Calendar APIs
        Calendar calendar = Calendar.getInstance();
        book.setUpdatedDate(calendar.getTime());
        
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        Book book = getBookById(id);
        bookRepository.delete(book);
    }

    @Transactional(readOnly = true)
    public List<Book> getBooksByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }

    @Transactional(readOnly = true)
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    @Transactional(readOnly = true)
    public List<Book> getRecentBooks(int daysAgo) {
        // Using deprecated Calendar API to calculate date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -daysAgo);
        Date cutoffDate = calendar.getTime();
        
        return bookRepository.findRecentAvailableBooks(cutoffDate);
    }

    @Transactional(readOnly = true)
    public Long getAvailableBookCount() {
        return bookRepository.countByAvailableTrue();
    }

    @Transactional(readOnly = true)
    public List<Book> searchBooks(String keyword) {
        // Trim using commons-lang 2.x
        String trimmedKeyword = StringUtils.trim(keyword);
        if (StringUtils.isEmpty(trimmedKeyword)) {
            return getAllBooks();
        }
        return bookRepository.findByTitleContainingIgnoreCase(trimmedKeyword);
    }
}
