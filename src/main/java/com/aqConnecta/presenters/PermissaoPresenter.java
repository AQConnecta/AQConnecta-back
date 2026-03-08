package com.aqConnecta.presenters;

import com.aqConnecta.model.Permissao;

public record PermissaoPresenter(
    Integer id,
    // `"CLIENTE"` ou `"ADMIN"`.
    String descricao
) {
    public static PermissaoPresenter apresentar(Permissao permissao) {
        return new PermissaoPresenter(
            permissao.getId(),
            permissao.getDescricao()
        );
    }
}

