package com.aqConnecta.exception.autenticacao;

import com.aqConnecta.exception.base.NaoAutorizadoException;

public class LoginNecessarioException extends NaoAutorizadoException {
    private static final String defaultMessage = "Você precisa estar logado para continuar.";

    public LoginNecessarioException() {
        super(defaultMessage);
    }

    public LoginNecessarioException(String message) {
        super(message);
    }

    public LoginNecessarioException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoginNecessarioException(Throwable cause) {
        super(defaultMessage, cause);
    }
}
