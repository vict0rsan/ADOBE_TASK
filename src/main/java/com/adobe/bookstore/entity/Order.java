package com.adobe.bookstore.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@JsonSerialize
public class Order {

    @Id
    @Column(name = "id", nullable = false)
    private String id = UUID.randomUUID().toString();

    /*
        We could follow a different approach so each book
        could have its own quantity instead of sharing that
        amount with the other ones by removing the books
        and quantity attributes and replacing them with
        a List<Pair<Book, Integer> so our order could have
        a custom quantity for each book
     */

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Book> books;

    private Integer quantity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
