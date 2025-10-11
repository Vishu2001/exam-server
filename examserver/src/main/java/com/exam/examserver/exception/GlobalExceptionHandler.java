package com.exam.examserver.exception;

import com.exam.examserver.dto.ApiError;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest req) {
        String path = ((ServletWebRequest) req).getRequest().getRequestURI();
        String value = ex.getValue() == null ? "null" : ex.getValue().toString();
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid value";
        String msg = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", value, ex.getName(), expected);

        ApiError err = new ApiError(HttpStatus.BAD_REQUEST.value(), "INVALID_PARAMETER", msg, path);
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, WebRequest req){
        String path = ((ServletWebRequest) req).getRequest().getRequestURI();
        ApiError err = new ApiError(HttpStatus.NOT_FOUND.value(),"RESOURCE_NOT_FOUND",ex.getMessage(),path);
        return new ResponseEntity<>(err,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, WebRequest req){
        String path = ((ServletWebRequest)req).getRequest().getRequestURI();
        // use BAD_REQUEST.value() here
        ApiError err = new ApiError(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", ex.getMessage(), path);
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict (ConflictException ex,WebRequest req){
        String path = ((ServletWebRequest)req).getRequest().getRequestURI();
        ApiError err = new ApiError(HttpStatus.CONFLICT.value(),"CONFLICT", ex.getMessage(), path);
        return new ResponseEntity<>(err,HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex,WebRequest req){
        String path = ((ServletWebRequest)req).getRequest().getRequestURI();
        ApiError err = new ApiError(HttpStatus.UNPROCESSABLE_ENTITY.value(), "BUSINESS_ERROR",ex.getMessage(),path);
        return new ResponseEntity<>(err,HttpStatus.UNPROCESSABLE_ENTITY);
    }

    //validation error
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,WebRequest req){
        String path = ((ServletWebRequest) req).getRequest().getRequestURI();
        Map<String,String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,FieldError::getDefaultMessage,(a,b)->a));

        ApiError err = new ApiError(HttpStatus.UNPROCESSABLE_ENTITY.value(),"VALIDATION_FAILED","Validation Failed",path);
        err.setDetails(Map.of("fieldErrors",fieldErrors));
        return new ResponseEntity<>(err,HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleBadJson(HttpMessageNotReadableException ex, WebRequest req) {
        String path = ((ServletWebRequest) req).getRequest().getRequestURI();
        ApiError err = new ApiError(HttpStatus.BAD_REQUEST.value(), "MALFORMED_JSON", "Malformed JSON request", path);
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimistic(OptimisticLockingFailureException ex, WebRequest req) {
        String path = ((ServletWebRequest) req).getRequest().getRequestURI();
        ApiError err = new ApiError(HttpStatus.CONFLICT.value(), "OPTIMISTIC_LOCK", "Concurrent update error. Retry.", path);
        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, WebRequest req) {
        String path = ((ServletWebRequest) req).getRequest().getRequestURI();
        ApiError err = new ApiError(HttpStatus.METHOD_NOT_ALLOWED.value(), "METHOD_NOT_ALLOWED", ex.getMessage(), path);
        return new ResponseEntity<>(err, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest req) {
        // log full stack trace here using logger
        String path = ((ServletWebRequest) req).getRequest().getRequestURI();
        ApiError err = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", "An unexpected error occurred", path);
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
