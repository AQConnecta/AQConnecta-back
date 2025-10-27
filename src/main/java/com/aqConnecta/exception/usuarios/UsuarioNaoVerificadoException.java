package com.aqConnecta.exception.usuarios;

public class UsuarioNaoVerificadoException extends RuntimeException {
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
