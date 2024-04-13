package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.Usuario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private Usuario usuario;
    private String token;

    public LoginResponse(Usuario usuario, String token) {
        this.usuario = usuario;
        this.token = token;
    }
}
