package com.adobe.bookstore.model;

import com.adobe.bookstore.entity.Book;

import java.util.List;

public class OrderModel {

    private List<Book> books;
    private Integer quantity;

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
