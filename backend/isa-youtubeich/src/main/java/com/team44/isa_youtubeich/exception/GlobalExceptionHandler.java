package com.team44.isa_youtubeich.exception;

import com.team44.isa_youtubeich.dto.ErrorResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private Environment environment;

    private boolean isDevMode() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }
    private boolean isTestMode() { return Arrays.asList(environment.getActiveProfiles()).contains("test"); }

    private String getErrorMessage(String opaqueMessage, String detailedMessage) {
        return isDevMode() || isTestMode() ? detailedMessage : opaqueMessage;
    }

    private ResponseEntity<ErrorResponseDto> jsonError(HttpStatus status, String message) {
        ErrorResponseDto error = new ErrorResponseDto(
                status.value(),
                message,
                System.currentTimeMillis()
        );
        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(ValidationException ex) {
        return jsonError(HttpStatus.BAD_REQUEST, getErrorMessage("Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String detailedMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse(ex.getMessage());

        return jsonError(HttpStatus.BAD_REQUEST, getErrorMessage("Bad Request", detailedMessage));
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceConflictException(ResourceConflictException ex) {
        return jsonError(HttpStatus.BAD_REQUEST, getErrorMessage("Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(NonOpaqueException.class)
    public ResponseEntity<ErrorResponseDto> handleNonOpaqueException(NonOpaqueException ex) {
        return jsonError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex){
        return jsonError(
                HttpStatus.BAD_REQUEST,
                getErrorMessage(
                        "Bad Request",
                        String.format("Incorrect argument type '%s' for '%s'", ex.getValue(), ex.getName())
                )
        );
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(Exception ex) {
        return jsonError(HttpStatus.FORBIDDEN, getErrorMessage("Forbidden", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException ex) {
        return jsonError(HttpStatus.FORBIDDEN, getErrorMessage("Forbidden", ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResourceFoundException(NoResourceFoundException ex) {
        return jsonError(HttpStatus.NOT_FOUND, getErrorMessage("Not Found", ex.getMessage()));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleRateLimitExceededException(RateLimitExceededException ex) {
        return jsonError(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        return jsonError(HttpStatus.INTERNAL_SERVER_ERROR, getErrorMessage("Internal Server Error", ex.getMessage()));
    }
}
