package com.adobe.bookstore.exception.handler;

import com.adobe.bookstore.exception.OrderException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(OrderException.class)
    public final ResponseEntity<ApiError> handleOrderException(OrderException ex){
        ApiError error = new ApiError(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
