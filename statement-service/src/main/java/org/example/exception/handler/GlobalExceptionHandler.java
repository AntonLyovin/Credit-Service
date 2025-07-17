package org.example.exception.handler;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ErrorDetail> errors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.add(new ErrorDetail(
                    error.getField(),
                    String.valueOf(error.getRejectedValue()),
                    error.getDefaultMessage(),
                    error.getCode()
            ));
        });

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Ошибка валидации данных",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ApiResponse(responseCode = "400", description = "Invalid enum value",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleEnumExceptions(HttpMessageNotReadableException ex) {
        String errorMessage = ex.getMessage();
        String field = "unknown";
        String value = "unknown";

        if (errorMessage.contains("not one of the values accepted for Enum class")) {
            String[] parts = errorMessage.split("from String \"|\": not one of");
            if (parts.length >= 2) {
                value = parts[1];
                field = extractFieldName(errorMessage);
            }
            errorMessage = "Недопустимое значение для поля " + field + ". Допустимые значения: " +
                    getEnumValues(errorMessage);
        }

        List<ErrorDetail> errors = List.of(
                new ErrorDetail(field, value, errorMessage, "InvalidEnumValue")
        );

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Ошибка в данных",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    public String extractFieldName(String errorMessage) {
        try {
            return errorMessage.split("type `[^`]+` from String")[0]
                    .split("`")[1]
                    .split("\\.")[3];
        } catch (Exception e) {
            return "unknown";
        }
    }

    public String getEnumValues(String errorMessage) {
        try {
            String valuesPart = errorMessage.split("values accepted for Enum class: \\[")[1]
                    .split("]")[0];
            return valuesPart.replace(", ", ", ");
        } catch (Exception e) {
            return "не определены";
        }
    }

    @Schema(description = "Error response")
    public record ErrorResponse(
            @Schema(description = "Timestamp of error", example = "2023-05-15T14:30:45.123")
            LocalDateTime timestamp,

            @Schema(description = "HTTP status code", example = "400")
            int status,

            @Schema(description = "General error message", example = "Ошибка валидации данных")
            String message,

            @Schema(description = "List of error details")
            List<ErrorDetail> errors
    ) {}

    @Schema(description = "Error details")
    public record ErrorDetail(
            @Schema(description = "Field name", example = "gender")
            String field,

            @Schema(description = "Invalid value", example = "UNKNOWN")
            String value,

            @Schema(description = "Error message", example = "Недопустимое значение для поля gender")
            String message,

            @Schema(description = "Error code", example = "InvalidEnumValue")
            String code
    ) {}
}