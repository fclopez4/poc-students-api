package com.scotiabank.studentsapi.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.accept.MissingApiVersionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExcepcionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleWebExchangeBindException(WebExchangeBindException ex) {
        log.error("Validation exception occurred:", ex);

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.PRECONDITION_FAILED.value());
        errorResponse.put("message", "Validation failed");
        errorResponse.put("errors", errors);

        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.PRECONDITION_FAILED));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleDuplicateKeyException(DuplicateKeyException ex) {
        log.error("Duplicate key exception occurred:", ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.PRECONDITION_FAILED.value());
        errorResponse.put("message", ex.getMessage());

        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.PRECONDITION_FAILED));
    }

    @ExceptionHandler({ MissingApiVersionException.class, InvalidApiVersionException.class })
    public Mono<ResponseEntity<Map<String, Object>>> handleMissingApiVersionException(ResponseStatusException ex) {
        log.error("API version exception occurred:", ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("message", ex.getReason());

        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGeneralException(Exception ex) {
        log.error("Unhandled exception occurred:", ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("message", ex.getMessage());

        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
