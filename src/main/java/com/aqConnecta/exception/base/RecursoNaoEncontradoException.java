package com.aqConnecta.exception.base;

public class RecursoNaoEncontradoException extends RuntimeException {
    private final static String defaultErrorMessage = "Recurso não encontrado.";

    public RecursoNaoEncontradoException() {
        super(defaultErrorMessage);
    }

    public RecursoNaoEncontradoException(Throwable cause) {
        super(defaultErrorMessage, cause);
    }

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }

    public RecursoNaoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}
