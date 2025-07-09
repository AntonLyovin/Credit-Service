package org.example.exception.custom;

public class InvalidStatementIdException extends RuntimeException {
    public InvalidStatementIdException(String statementId) {
        super("Некорректный формат ID заявки: " + statementId);
    }
}