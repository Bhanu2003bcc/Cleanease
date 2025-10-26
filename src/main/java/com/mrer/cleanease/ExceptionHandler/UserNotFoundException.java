package com.mrer.cleanease.ExceptionHandler;


public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String message){
        super(message);
    }
}
