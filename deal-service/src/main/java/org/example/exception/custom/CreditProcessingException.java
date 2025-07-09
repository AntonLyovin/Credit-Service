package org.example.exception.custom;

public class CreditProcessingException extends RuntimeException {
    public CreditProcessingException(String message) {
        super(message);
    }
}