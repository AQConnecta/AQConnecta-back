package com.aqConnecta.exception.usuarios;

import com.aqConnecta.exception.base.AcaoProibidaException;

public class UsuarioNaoVerificadoException extends AcaoProibidaException {
    private static String formatErrorMessage(String email) {
        return "Usuário não foi ativado, verifique seu email: " + email;
    }

    public UsuarioNaoVerificadoException(String email) {
        super(formatErrorMessage(email));
    }

    public UsuarioNaoVerificadoException(String email, Throwable cause) {
        super(formatErrorMessage(email), cause);
    }
}
