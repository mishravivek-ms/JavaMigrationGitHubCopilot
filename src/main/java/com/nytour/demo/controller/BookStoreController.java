package com.nytour.demo.controller;

import com.nytour.demo.model.Book;
import com.nytour.demo.service.BookService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BookStore Controller - Legacy Spring Boot 2.7.x patterns
 * Following the same pattern as MessageController
 * 
 * Provides CRUD operations for book store:
 * - GET /api/bookstore - Get all books
 * - GET /api/bookstore/{id} - Get book by ID
 * - POST /api/bookstore - Create new book
 * - PUT /api/bookstore/{id} - Update existing book
 * - DELETE /api/bookstore/{id} - Delete book
 * - GET /api/bookstore/search - Search books by title
 * - GET /api/bookstore/author/{author} - Get books by author
 */
@Controller
@RequestMapping("/api/bookstore")
public class BookStoreController {

    // Log4j 1.x (deprecated, migrate to SLF4J)
    private static final Logger logger = Logger.getLogger(BookStoreController.class);

    // Field injection (legacy pattern)
    @Autowired
    private BookService bookService;

    // SimpleDateFormat (not thread-safe, deprecated pattern)
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Get all books - Using @ResponseBody to return JSON
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllBooks(HttpServletRequest request) {
        logger.info("GET /bookstore - Fetching all books");
        
        try {
            List<Book> books = bookService.getAllBooks();
            
            // Manual response building (legacy pattern)
            Map<String, Object> response = new HashMap<String, Object>();
            response.put("status", "success");
            response.put("data", books);
            response.put("count", books.size());
            response.put("timestamp", dateFormat.format(new Date()));
            
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching books", e);
            return handleError("Failed to fetch books", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get book by ID
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBookById(@PathVariable("id") Long id) {
        logger.info("GET /bookstore/" + id);
        
        try {
            Book book = bookService.getBookById(id);
            
            Map<String, Object> responseMap = new HashMap<String, Object>();
            responseMap.put("status", "success");
            responseMap.put("data", book);
            
            return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Book not found: " + id, e);
            return handleError("Book not found with id: " + id, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Create new book - Using @Valid with javax.validation
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createBook(
            @Valid @RequestBody CreateBookRequest request) {
        
        logger.info("POST /bookstore - Creating book: " + request.getTitle());
        
        try {
            Book book = bookService.createBook(
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getPrice()
            );
            
            Map<String, Object> response = new HashMap<String, Object>();
            response.put("status", "success");
            response.put("message", "Book created successfully");
            response.put("data", book);
            response.put("createdAt", dateFormat.format(new Date()));
            
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid book data", e);
            return handleError(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update book
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBook(
            @PathVariable("id") Long id,
            @RequestBody UpdateBookRequest request) {
        
        logger.info("PUT /bookstore/" + id);
        
        try {
            Book book = bookService.updateBook(
                id,
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getPrice()
            );
            
            Map<String, Object> response = new HashMap<String, Object>();
            response.put("status", "success");
            response.put("message", "Book updated successfully");
            response.put("data", book);
            
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error updating book", e);
            return handleError("Failed to update book", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete book
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteBook(@PathVariable("id") Long id) {
        logger.info("DELETE /bookstore/" + id);
        
        try {
            bookService.deleteBook(id);
            
            Map<String, Object> response = new HashMap<String, Object>();
            response.put("status", "success");
            response.put("message", "Book deleted successfully");
            
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error deleting book", e);
            return handleError("Failed to delete book", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Search books by title
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchBooks(
            @RequestParam(value = "keyword", required = false) String keyword) {
        
        logger.info("GET /bookstore/search?keyword=" + keyword);
        
        List<Book> books = bookService.searchBooks(keyword);
        
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", "success");
        response.put("data", books);
        response.put("count", books.size());
        
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    /**
     * Get books by author
     */
    @RequestMapping(value = "/author/{author}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBooksByAuthor(@PathVariable("author") String author) {
        logger.info("GET /bookstore/author/" + author);
        
        List<Book> books = bookService.getBooksByAuthor(author);
        
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", "success");
        response.put("data", books);
        response.put("author", author);
        
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    // Helper method for error responses
    private ResponseEntity<Map<String, Object>> handleError(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<String, Object>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        errorResponse.put("timestamp", dateFormat.format(new Date()));
        
        return new ResponseEntity<Map<String, Object>>(errorResponse, status);
    }

    // Inner classes for request DTOs (legacy pattern, could be separate files)
    
    public static class CreateBookRequest {
        @javax.validation.constraints.NotNull
        @javax.validation.constraints.Size(min = 1, max = 200)
        private String title;
        
        @javax.validation.constraints.NotNull
        @javax.validation.constraints.Size(min = 1, max = 100)
        private String author;
        
        private String isbn;
        
        private Double price;

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
    }

    public static class UpdateBookRequest {
        private String title;
        
        private String author;
        
        private String isbn;
        
        private Double price;

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
    }
}
