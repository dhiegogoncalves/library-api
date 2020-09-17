package com.project.libraryapi.api.exceptions;

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = -2732668308035360645L;

    public BusinessException(String message) {
        super(message);
    }
}
