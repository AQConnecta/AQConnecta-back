package com.aqConnecta.exception.usuarios;

public class UsuarioRemovidoException extends RuntimeException {
    private final static String errorMessage = "Usuário não existe mais";

    public UsuarioRemovidoException() {
        super(errorMessage);
    }

    public UsuarioRemovidoException(Throwable cause) {
        super(errorMessage, cause);
    }
}
