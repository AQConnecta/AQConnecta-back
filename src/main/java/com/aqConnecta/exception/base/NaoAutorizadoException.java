package com.aqConnecta.exception.base;

public class NaoAutorizadoException extends RuntimeException {
    private static final String defaultMessage = "Não autorizado.";

    public NaoAutorizadoException() {
        super(defaultMessage);
    }

    public NaoAutorizadoException(String message) {
        super(message);
    }

    public NaoAutorizadoException(String message, Throwable cause) {
        super(message, cause);
    }

    public NaoAutorizadoException(Throwable cause) {
        super(defaultMessage, cause);
    }
}
