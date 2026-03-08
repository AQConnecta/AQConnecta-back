package com.aqConnecta.presenters;

import com.aqConnecta.model.Permissao;
import com.aqConnecta.model.Usuario;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UsuarioAutorPresenter(
    @NotNull String id,
    @NotNull String email,
    @NotNull String nome,
    @NotNull Set<Permissao> permissao,
    @NotNull Boolean deletado,
    @NotNull Boolean ativado,
    String fotoPerfil,
    @NotNull String userUrl
) {
    public static UsuarioAutorPresenter apresentar(Usuario autor) {
        return new UsuarioAutorPresenter(
            autor.getId().toString(),
            autor.getEmail(),
            autor.getNome(),
            autor.getPermissao(),
            autor.getDeletado(),
            autor.getAtivado(),
            autor.getFotoPerfil(),
            autor.getUserUrl()
        );
    }
}
