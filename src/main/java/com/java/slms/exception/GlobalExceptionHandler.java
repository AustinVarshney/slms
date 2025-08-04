package com.java.slms.exception;

import com.java.slms.payload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ApiResponse<String>> handleStudentAlreadyExist(AlreadyExistException ex)
    {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .data(null).message(ex.getMessage()).status(HttpStatus.CONFLICT.value()).build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException ex)
    {
        ApiResponse<?> response = ApiResponse.builder()
                .data(null)
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
