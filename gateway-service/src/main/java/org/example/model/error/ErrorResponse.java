package org.example.model.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

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
) {
}
