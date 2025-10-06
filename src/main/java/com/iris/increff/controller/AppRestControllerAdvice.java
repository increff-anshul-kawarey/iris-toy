package com.iris.increff.controller;

import com.iris.increff.model.MessageData;
import com.iris.increff.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppRestControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(AppRestControllerAdvice.class);

    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageData handle(ApiException e) {
        logger.warn("Handled ApiException: {}", e.getMessage());
        MessageData data = new MessageData();
        data.setMessage(e.getMessage());
        return data;
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageData handle(Throwable e) {
        logger.error("Unhandled exception", e);
        MessageData data = new MessageData();
        data.setMessage("An unknown error has occurred");
        return data;
    }
}