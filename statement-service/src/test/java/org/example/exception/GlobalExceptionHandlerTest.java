package org.example.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import org.example.exception.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        objectMapper = new ObjectMapper();
    }

    @Test
    void handleValidationExceptions_ShouldReturnProperResponse() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError(
                "objectName",
                "fieldName",
                "invalidValue",
                false,
                null,
                null,
                "default message"
        );
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ошибка валидации данных", response.getBody().message());
        assertEquals(1, response.getBody().errors().size());

        GlobalExceptionHandler.ErrorDetail errorDetail = response.getBody().errors().get(0);
        assertEquals("fieldName", errorDetail.field());
        assertEquals("invalidValue", errorDetail.value());
        assertEquals("default message", errorDetail.message());
    }

    @Test
    void handleEnumExceptions_ShouldReturnProperResponse_ForEnumError() {
        String errorMessage = "JSON parse error: Cannot deserialize value of type `com.example.Gender` " +
                "from String \"UNKNOWN\": not one of the values accepted for Enum class: [MALE, FEMALE]";
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                errorMessage, null, null);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleEnumExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ошибка в данных", response.getBody().message());
        assertEquals(1, response.getBody().errors().size());

        GlobalExceptionHandler.ErrorDetail errorDetail = response.getBody().errors().get(0);
        assertTrue(errorDetail.field().matches("Gender|unknown"));
        assertEquals("UNKNOWN", errorDetail.value());
        assertTrue(errorDetail.message().contains("Недопустимое значение"));
        assertTrue(errorDetail.message().contains("MALE, FEMALE"));
    }

    @Test
    void handleEnumExceptions_ShouldReturnGenericResponse_ForNonEnumError() {
        String errorMessage = "Generic JSON parse error";
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                errorMessage, null, null);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleEnumExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ошибка в данных", response.getBody().message());
        assertEquals(1, response.getBody().errors().size());

        GlobalExceptionHandler.ErrorDetail errorDetail = response.getBody().errors().get(0);
        assertEquals("unknown", errorDetail.field());
        assertEquals("unknown", errorDetail.value());
        assertEquals(errorMessage, errorDetail.message());
    }

    @Test
    void errorResponseRecord_ShouldHaveCorrectSchemaAnnotations() {
        assertNotNull(GlobalExceptionHandler.ErrorResponse.class.getAnnotation(Schema.class));

        try {
            Schema schema = GlobalExceptionHandler.ErrorResponse.class.getDeclaredField("timestamp")
                    .getAnnotation(Schema.class);
            assertNotNull(schema);
            assertEquals("Timestamp of error", schema.description());

            schema = GlobalExceptionHandler.ErrorResponse.class.getDeclaredField("status")
                    .getAnnotation(Schema.class);
            assertNotNull(schema);
            assertEquals("HTTP status code", schema.description());

            schema = GlobalExceptionHandler.ErrorResponse.class.getDeclaredField("message")
                    .getAnnotation(Schema.class);
            assertNotNull(schema);
            assertEquals("General error message", schema.description());
        } catch (NoSuchFieldException e) {
            fail("Field not found in ErrorResponse record");
        }
    }

    @Test
    void errorDetailRecord_ShouldHaveCorrectSchemaAnnotations() {
        assertNotNull(GlobalExceptionHandler.ErrorDetail.class.getAnnotation(Schema.class));

        try {
            Schema schema = GlobalExceptionHandler.ErrorDetail.class.getDeclaredField("field")
                    .getAnnotation(Schema.class);
            assertNotNull(schema);
            assertEquals("Field name", schema.description());

            schema = GlobalExceptionHandler.ErrorDetail.class.getDeclaredField("value")
                    .getAnnotation(Schema.class);
            assertNotNull(schema);
            assertEquals("Invalid value", schema.description());

            schema = GlobalExceptionHandler.ErrorDetail.class.getDeclaredField("message")
                    .getAnnotation(Schema.class);
            assertNotNull(schema);
            assertEquals("Error message", schema.description());

            schema = GlobalExceptionHandler.ErrorDetail.class.getDeclaredField("code")
                    .getAnnotation(Schema.class);
            assertNotNull(schema);
            assertEquals("Error code", schema.description());
        } catch (NoSuchFieldException e) {
            fail("Field not found in ErrorDetail record");
        }
    }

    @Test
    void extractFieldName_ShouldReturnUnknown_ForInvalidFormat() {
        String errorMessage = "Invalid error message format";
        String fieldName = exceptionHandler.extractFieldName(errorMessage);
        assertEquals("unknown", fieldName);
    }

    @Test
    void getEnumValues_ShouldExtractEnumValues() {
        String errorMessage = "values accepted for Enum class: [MALE, FEMALE, OTHER]";
        String enumValues = exceptionHandler.getEnumValues(errorMessage);
        assertEquals("MALE, FEMALE, OTHER", enumValues);
    }

    @Test
    void getEnumValues_ShouldReturnDefault_ForInvalidFormat() {
        String errorMessage = "Invalid error message format";
        String enumValues = exceptionHandler.getEnumValues(errorMessage);
        assertEquals("не определены", enumValues);
    }
}