package com.exam.examserver.exception;

public class ResouceNotFoundException extends RuntimeException{
    public ResouceNotFoundException(String message){
        super(message);
    }
}
