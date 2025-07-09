package org.example.exception.custom;

public class ScoringServiceException extends RuntimeException {
    public ScoringServiceException(String message) {
        super(message);
    }
}