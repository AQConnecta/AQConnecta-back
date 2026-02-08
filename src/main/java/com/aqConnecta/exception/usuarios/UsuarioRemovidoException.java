package com.aqConnecta.exception.usuarios;

import com.aqConnecta.exception.base.AcaoProibidaException;

public class UsuarioRemovidoException extends AcaoProibidaException {
    private final static String errorMessage = "Usuário não existe mais.";

    public UsuarioRemovidoException() {
        super(errorMessage);
    }
}
