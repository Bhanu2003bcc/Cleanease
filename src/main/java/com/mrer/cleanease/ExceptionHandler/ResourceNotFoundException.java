package com.mrer.cleanease.ExceptionHandler;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String msg ){
        super (msg);
    }
}
