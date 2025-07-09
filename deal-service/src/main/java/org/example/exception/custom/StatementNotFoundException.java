package org.example.exception.custom;

import lombok.Getter;

public class StatementNotFoundException extends RuntimeException {
    private final String statementId;

    public StatementNotFoundException(String statementId) {
        super("Statement with id " + statementId + " not found");
        this.statementId = statementId;
    }

    public String getStatementId() {
        return statementId;
    }
}