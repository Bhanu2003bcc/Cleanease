package com.mrer.cleanease.ExceptionHandler;

public class BusinessException extends RuntimeException{

    public BusinessException(String msg){
        super(msg);
    }
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
