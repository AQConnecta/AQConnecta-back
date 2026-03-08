package com.aqConnecta.presenters;

import com.aqConnecta.model.Permissao;
import jakarta.validation.constraints.NotNull;

enum PermissaoDescricao {
    CLIENTE,
    ADMIN
}

public record PermissaoPresenter(
    @NotNull Integer id,
    @NotNull PermissaoDescricao descricao
) {
    public static PermissaoPresenter apresentar(Permissao permissao) {
        return new PermissaoPresenter(
            permissao.getId(),
            PermissaoDescricao.valueOf(permissao.getDescricao())
        );
    }
}

