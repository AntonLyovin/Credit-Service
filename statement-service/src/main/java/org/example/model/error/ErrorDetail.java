package org.example.model.error;

import io.swagger.v3.oas.annotations.media.Schema;

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
) {
}
