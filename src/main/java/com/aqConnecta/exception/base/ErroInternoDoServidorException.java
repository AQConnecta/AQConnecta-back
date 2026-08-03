package com.aqConnecta.exception.base;

public class ErroInternoDoServidorException extends RuntimeException {
    private static final String defaultMessage = "Houve um problema no nosso servidor. Tente novamente mais tarde.";

    public ErroInternoDoServidorException() {
        super(defaultMessage);
    }

    public ErroInternoDoServidorException(Throwable cause) {
        super(defaultMessage, cause);
    }

    public ErroInternoDoServidorException(String message) {
        super(message);
    }

    public ErroInternoDoServidorException(String message, Throwable cause) {

    }
}
