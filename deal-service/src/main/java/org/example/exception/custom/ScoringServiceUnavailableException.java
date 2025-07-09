package org.example.exception.custom;

public class ScoringServiceUnavailableException extends RuntimeException {
    public ScoringServiceUnavailableException(String message) {
        super(message);
    }
}