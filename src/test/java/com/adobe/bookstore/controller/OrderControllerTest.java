package com.adobe.bookstore.controller;

import com.adobe.bookstore.entity.Book;
import com.adobe.bookstore.entity.BookStock;
import com.adobe.bookstore.entity.Order;
import com.adobe.bookstore.model.OrderModel;
import com.adobe.bookstore.repository.BookStockRepository;
import com.adobe.bookstore.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.CoreMatchers.is;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = OrderController.class)
public class OrderControllerTest {
    private final static Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

    private final static Integer BAD_REQUEST_VALUE = HttpStatus.BAD_REQUEST.value();
    private final static Integer OK_VALUE = HttpStatus.OK.value();
    private final static Integer DEFAULT_QUANTITY = 2;
    private final static Integer BOOK_STOCK = 5;
    private final static Integer MORE_THAN_STOCK_QUANTITY = 7;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookStockRepository bookStockRepository;

    @MockBean
    private OrderRepository orderRepository;

    @Test
    void validateSuccessOrderWithUniqueId() throws Exception {
        //given an order
        Mockito.when(bookStockRepository.findByName(any(String.class))).thenReturn(getBookStockObject());
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(getOrderObject());

        //when the order is posted
        MvcResult mcvResult = mockMvc.perform(post("/orders/")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(formOrderModel(DEFAULT_QUANTITY))))
                        .andReturn();

        //then the gets the order id
        String actualResponseBody = mcvResult.getResponse().getContentAsString();
        assertThat(actualResponseBody).matches(UUID_REGEX_PATTERN);
    }

    @Test
    void validateIfNotEnoughOrder() throws Exception {
        //given book that has not enough stock
        Mockito.when(bookStockRepository.findByName(any(String.class))).thenReturn(getBookStockObject());
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(getOrderObject());

        //when that book is included in a posted order
        MvcResult mvcResult = mockMvc.perform(post("/orders/")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(formOrderModel(MORE_THAN_STOCK_QUANTITY))))
                        .andReturn();

        //Then an exception is thrown
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(BAD_REQUEST_VALUE);
        assertThat(actualResponseBody).contains("Not stock available for the book: ");
    }

    @Test
    void validateIfBookStockIsNull() throws Exception {
        //given book that has a null bookStore
        Mockito.when(bookStockRepository.findByName(any(String.class))).thenReturn(null);

        //when that book is included in a posted order
        MvcResult mvcResult = mockMvc.perform(post("/orders/")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(formOrderModel(DEFAULT_QUANTITY))))
                        .andReturn();

        //Then an exception is thrown
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(BAD_REQUEST_VALUE);
        assertThat(actualResponseBody).contains("Invalid book name");
    }

    @Test
    void validateGetOrderApi() throws Exception {
        //given a list of orders
        Mockito.when(bookStockRepository.findByName(any(String.class))).thenReturn(getBookStockObject());
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(getOrderObject());
        Mockito.when(orderRepository.findAll()).thenReturn(getOrderList());

        mockMvc.perform(post("/orders/")
                        .contentType("applications/json")
                        .content(objectMapper.writeValueAsString(formOrderModel(DEFAULT_QUANTITY))))
                        .andReturn();

        //when we execute the get call and get the expected values
        MvcResult mvcResult = mockMvc.perform(get("/orders/")
                        .contentType("application/json"))
                        .andExpect(jsonPath("$[0].quantity", is(DEFAULT_QUANTITY)))
                        .andExpect(jsonPath("$[0].books[0].title", is("nulla qui proident consectetur occaecat")))
                        .andReturn();

        //then we get a successful http response
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(OK_VALUE);

    }

    @Test
    void validateSuccessStockUpdating() throws Exception {
        //given an order
        Mockito.when(bookStockRepository.findByName(any(String.class))).thenReturn(getBookStockObject());
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(getOrderObject());

        //when the order is posted without errors
        mockMvc.perform(post("/orders/")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(formOrderModel(DEFAULT_QUANTITY))))
                .andReturn();

        //then the stock is properly updated
        BookStock bookStock = bookStockRepository.findByName(formOrderModel(DEFAULT_QUANTITY).getBooks().get(0).getTitle());
        assertThat(bookStock.getQuantity()).isEqualTo(BOOK_STOCK - DEFAULT_QUANTITY);
    }

    @Test
    void validateStockUpdatingFails() throws Exception {
        //given an order which stock cannot be updated
        Mockito.when(bookStockRepository.findByName(any(String.class))).thenReturn(getBookStockObject());
        Mockito.when(bookStockRepository.save(any(BookStock.class))).thenThrow(RuntimeException.class);
        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(getOrderObject());

        //when the order is posted without errors
        MvcResult mvcResult = mockMvc.perform(post("/orders/")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(formOrderModel(DEFAULT_QUANTITY))))
                        .andReturn();

        //then the user keeps getting the order id
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        assertThat(actualResponseBody).matches(UUID_REGEX_PATTERN);
    }


    private OrderModel formOrderModel(int quantity) {
        OrderModel model = new OrderModel();
        Book book = new Book();
        book.setTitle("nulla qui proident consectetur occaecat");

        List<Book> bookList = List.of(book);
        model.setBooks(bookList);
        model.setQuantity(quantity);

        return model;
    }

    private BookStock getBookStockObject() {
        BookStock stock = new BookStock();
        stock.setId(UUID.randomUUID().toString());
        stock.setName("nulla qui proident consectetur occaecat");
        stock.setQuantity(BOOK_STOCK);
        return stock;
    }

    private Order getOrderObject() {
        Book book = new Book();
        book.setTitle("nulla qui proident consectetur occaecat");

        List<Book> bookList = List.of(book);

        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setBooks(bookList);
        order.setQuantity(DEFAULT_QUANTITY);
        return order;
    }

    private List<Order> getOrderList() {
        Order order = getOrderObject();
        List<Order> orderList = List.of(order);
        return orderList;
    }
}
