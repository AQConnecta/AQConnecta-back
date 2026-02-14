package com.aqConnecta.exception.base;

public class AcaoProibidaException extends RuntimeException {
    private static final String defaultMessage = "Proibido.";

    public AcaoProibidaException() {
        super(defaultMessage);
    }

    public AcaoProibidaException(Throwable cause) {
        super(defaultMessage, cause);
    }

    public AcaoProibidaException(String message) {
        super(message);
    }

    public AcaoProibidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
