package com.aqConnecta.presenters;

import com.aqConnecta.model.Permissao;
import com.aqConnecta.model.Usuario;

import java.util.Set;

public record UsuarioAutorPresenter(
    String id,
    String email,
    String nome,
    Set<Permissao> permissao,
    Boolean deletado,
    Boolean ativado,
    String fotoPerfil,
    String userUrl
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
