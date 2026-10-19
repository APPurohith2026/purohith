package com.purohitcommunity.portal.web;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class})
  ResponseEntity<?> bad(RuntimeException ex){ return ResponseEntity.badRequest().body(Map.of("timestamp",Instant.now(),"message",ex.getMessage())); }
  @ExceptionHandler(NoSuchElementException.class)
  ResponseEntity<?> notFound(NoSuchElementException ex){ return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("timestamp",Instant.now(),"message","Resource not found")); }
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException ex){ var errors=ex.getBindingResult().getFieldErrors().stream().collect(java.util.stream.Collectors.toMap(e->e.getField(),e->Optional.ofNullable(e.getDefaultMessage()).orElse("Invalid"),(a,b)->a)); return ResponseEntity.badRequest().body(Map.of("message","Validation failed","errors",errors)); }
}
