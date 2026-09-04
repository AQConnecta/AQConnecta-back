package com.aqConnecta.exception;

import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.exception.usuarios.UsuarioNaoVerificadoException;
import com.aqConnecta.exception.usuarios.UsuarioRemovidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<?> handleException(RecursoNaoEncontradoException ex) {
        return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsuarioRemovidoException.class)
    public ResponseEntity<?> handleException(UsuarioRemovidoException ex) {
        return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UsuarioNaoVerificadoException.class)
    public ResponseEntity<?> handleException(UsuarioNaoVerificadoException ex) {
        return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseHandler.generateResponse("Erro de validação", HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseHandler.generateResponse("Acesso negado: permissão insuficiente", HttpStatus.FORBIDDEN);
    }
}
