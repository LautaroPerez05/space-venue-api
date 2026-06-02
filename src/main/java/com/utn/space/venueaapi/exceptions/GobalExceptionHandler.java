package com.utn.space.venueaapi.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GobalExceptionHandler {

    @ExceptionHandler(ExceptionIdNotFound.class)
    public ResponseEntity<String> idNotFound (ExceptionIdNotFound e){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(ExceptionInvalidDate.class)
    public ResponseEntity<String> idNotFound (ExceptionInvalidDate e){
        return ResponseEntity
                .badRequest()
                .body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)///este lo copie de GPT
    public ResponseEntity<Map<String, String>> manejarValidaciones(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errores.put(error.getField(),
                                error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errores);
    }
}
