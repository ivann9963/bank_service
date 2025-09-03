// src/main/java/smartit_task/bank_service/api/ApiExceptionHandler.java
package smartit_task.bank_service.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (a,b) -> a,
                        LinkedHashMap::new
                ));
        String message = errors.values().stream().findFirst().orElse("Validation failed");
        return ResponseEntity.badRequest().body(Map.of("message", message, "errors", errors));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,Object>> handleRSE(ResponseStatusException ex) {
        String msg = ex.getReason() != null ? ex.getReason() : "Request failed";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> handleIAE(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,Object>> handleDataConflict(DataIntegrityViolationException ex) {
        String mostSpecific = Optional.ofNullable(ex.getMostSpecificCause())
                .map(Throwable::getMessage)
                .orElse("");
        String msg = "Conflict";
        if (mostSpecific.contains("accounts_name_key") || mostSpecific.contains("uk_accounts_name")) {
            msg = "Account name already exists.";
        } else if (mostSpecific.contains("accounts_iban_key") || mostSpecific.contains("uk_accounts_iban")) {
            msg = "Account IBAN already exists.";
        } else if (mostSpecific.contains("uk_transfers_idem")) {
            msg = "Duplicate idempotency key: this transfer request was already processed.";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", msg));
    }
}
