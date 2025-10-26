package com.mrer.cleanease.ExceptionHandler;

import com.mrer.cleanease.dto.response.ApiResponse;
import com.mrer.cleanease.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFound(UserNotFoundException e) {
        log.warn("User not found: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadyExists(UserAlreadyExistException e) {
        log.warn("User already exists: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN,
                "Access denied: You don't have permission to access this resource", null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException e) {
        log.warn("Bad credentials: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password", null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthentication(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication failed", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach((FieldError error) ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
        log.error("Business rule violation: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", null);
    }

    //----Stripe Exceptions----//

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException e) {
        log.error("Payment error: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Payment Error")
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    // 🔹 Helper method for consistent error response
    private ResponseEntity<ApiResponse<Object>> buildErrorResponse(HttpStatus status,
                                                                   String message,
                                                                   Object errors) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
