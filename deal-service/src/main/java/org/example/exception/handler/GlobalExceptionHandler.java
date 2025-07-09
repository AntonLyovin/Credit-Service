package org.example.exception.handler;

import org.example.exception.custom.InvalidStatementIdException;
import org.example.exception.custom.ScoringServiceException;
import org.example.exception.custom.ScoringServiceUnavailableException;
import org.example.exception.custom.StatementNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(StatementNotFoundException.class)
    public ProblemDetail handleStatementNotFound(StatementNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Statement Not Found");
        problemDetail.setType(URI.create("https://api.example.com/errors/not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        // Убедитесь, что StatementNotFoundException имеет getStatementId()
        if (ex.getStatementId() != null) {
            problemDetail.setProperty("statementId", ex.getStatementId());
            logger.error("Statement not found: ID {}", ex.getStatementId());
        } else {
            logger.error("Statement not found: {}", ex.getMessage());
        }

        return problemDetail;
    }

    @ExceptionHandler(InvalidStatementIdException.class)
    public ProblemDetail handleInvalidStatementId(InvalidStatementIdException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Invalid Statement ID");
        problemDetail.setType(URI.create("https://api.example.com/errors/invalid-input"));
        problemDetail.setProperty("timestamp", Instant.now());

        logger.warn("Invalid statement ID: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(ScoringServiceUnavailableException.class)
    public ProblemDetail handleScoringServiceUnavailable(
            ScoringServiceUnavailableException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                ex.getMessage()
        );
        problemDetail.setTitle("Scoring Service Unavailable");
        problemDetail.setType(URI.create("https://api.example.com/errors/service-unavailable"));
        problemDetail.setProperty("timestamp", Instant.now());

        logger.error("Scoring service unavailable: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(ScoringServiceException.class)
    public ProblemDetail handleScoringServiceError(
            ScoringServiceException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Scoring Service Error");
        problemDetail.setType(URI.create("https://api.example.com/errors/bad-request"));
        problemDetail.setProperty("timestamp", Instant.now());

        logger.error("Scoring service error: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllExceptions(Exception ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error"
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://api.example.com/errors/internal-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        logger.error("Internal server error: {}", ex.getMessage(), ex);
        return problemDetail;
    }
}
