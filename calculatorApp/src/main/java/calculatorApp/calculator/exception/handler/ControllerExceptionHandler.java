package calculatorApp.calculator.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.info("Обработка ошибки валидации");
        List<Map<String, String>> errorList = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("field", error.getField());
            errorMap.put("message", error.getDefaultMessage());
            errorList.add(errorMap);
        }
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("errors", errorList);
        return ResponseEntity.badRequest().body(responseBody);
    }
}
