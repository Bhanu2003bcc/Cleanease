package com.mrer.cleanease.ExceptionHandler;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PaymentException extends RuntimeException{
    public PaymentException(String msg){
        super(msg);
    }
    public PaymentException (String msg, Throwable throwable ){
        super(msg, throwable);
    }
}
