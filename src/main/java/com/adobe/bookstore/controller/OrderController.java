package com.adobe.bookstore.controller;

import com.adobe.bookstore.entity.BookStock;
import com.adobe.bookstore.entity.Order;
import com.adobe.bookstore.exception.OrderException;
import com.adobe.bookstore.model.OrderModel;
import com.adobe.bookstore.repository.BookStockRepository;
import com.adobe.bookstore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/orders/")
public class OrderController {

    @Autowired
    private BookStockRepository bookStockRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderModel newOrder){

        newOrder.getBooks().stream().forEach(book -> {
            BookStock currentStock = bookStockRepository.findByName(book.getTitle());
            if(Objects.nonNull(currentStock)){
                if(currentStock.getQuantity() < newOrder.getQuantity()) {
                    throw new OrderException("Not stock available for the book: " + book.getTitle());
                }
            }else{
                throw new OrderException("Invalid book name");
            }
        });

        updateStock(newOrder);
        Order order = new Order();
        order.setBooks(newOrder.getBooks());
        order.setQuantity(newOrder.getQuantity());
        Order orderFromDb = orderRepository.save(order);
        return ResponseEntity.ok(orderFromDb.getId());
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(){
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @Async
    void updateStock(OrderModel newOrder){
        try{
            newOrder.getBooks().stream().forEach(book -> {
                BookStock currentStock = bookStockRepository.findByName(book.getTitle());
                currentStock.setQuantity(currentStock.getQuantity() - newOrder.getQuantity());
                bookStockRepository.save(currentStock);
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
