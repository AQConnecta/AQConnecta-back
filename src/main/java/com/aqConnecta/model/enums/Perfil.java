package com.aqConnecta.model.enums;

import lombok.Getter;

@Getter
public enum Perfil {

    CLIENTE(1, "ROLE_CLIENTE"), ADMIN(2, "ROLE_ADMIN");

    private Integer codigo;

    private String descricao;

    private Perfil(Integer codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static Perfil toEnum(Integer cod) {
        if(cod == null) {
            return null;
        }

        for(Perfil x : Perfil.values()) {
            if(cod.equals(x.getCodigo())){
                return x;
            }
        }

        throw new IllegalArgumentException("Perfil inválido");
    }
}