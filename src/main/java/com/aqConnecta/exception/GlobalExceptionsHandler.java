package com.aqConnecta.exception;

import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.exception.base.AcaoProibidaException;
import com.aqConnecta.exception.base.NaoAutorizadoException;
import com.aqConnecta.exception.base.RecursoNaoEncontradoException;
import com.aqConnecta.exception.usuarios.UsuarioNaoVerificadoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<?> handleException(RecursoNaoEncontradoException ex) {
        return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AcaoProibidaException.class)
    public ResponseEntity<?> handleException(AcaoProibidaException ex) {
        return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NaoAutorizadoException.class)
    public ResponseEntity<?> handleException(NaoAutorizadoException ex) {
        return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsuarioNaoVerificadoException.class)
    public ResponseEntity<?> handleException(UsuarioNaoVerificadoException ex) {
        return ResponseHandler.generateResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUncaughtException(Exception ex) {
        log.error("Erro não tratado: '{}'.", ex.getMessage(), ex);

        return ResponseHandler.generateResponse("Houve um problema no nosso servidor. Tente novamente mais tarde.",
            HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
