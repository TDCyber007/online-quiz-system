package com.quizsystem.service;

/** Thrown when user input does not pass the business rules. */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
