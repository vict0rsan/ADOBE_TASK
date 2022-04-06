package com.adobe.bookstore.controller;

import com.adobe.bookstore.entity.BookStock;
import com.adobe.bookstore.repository.BookStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books_stock/")
public class BookStockController {

    private BookStockRepository bookStockRepository;

    @Autowired
    public BookStockController(BookStockRepository bookStockRepository) {
        this.bookStockRepository = bookStockRepository;
    }

    @GetMapping("{bookId}")
    public ResponseEntity<BookStock> getStockById(@PathVariable String bookId) {
        return bookStockRepository.findById(bookId)
                .map(bookStock -> ResponseEntity.ok(bookStock))
                .orElse(ResponseEntity.notFound().build());
    }
}
