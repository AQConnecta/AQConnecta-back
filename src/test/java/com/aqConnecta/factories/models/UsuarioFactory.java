package com.aqConnecta.factories.models;

import com.aqConnecta.model.Permissao;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.utils.SlugUtils;
import net.datafaker.Faker;

import java.util.HashSet;
import java.util.UUID;

public class UsuarioFactory {
    public static Usuario criar() {
        return UsuarioFactory.criar(false);
    }

    public static Usuario criarAdministrador() {
        return UsuarioFactory.criar(true).toBuilder().ativado(true).build();
    }

    public static Usuario criarAtivado() {
        return UsuarioFactory.criar(false).toBuilder().ativado(true).build();
    }

    private static Usuario criar(boolean ehAdmin) {
        final var permissions = new HashSet<Permissao>();

        if (ehAdmin) permissions.add(new Permissao(2, Permissao.ROLE_ADMIN));
        else permissions.add(new Permissao(1, Permissao.ROLE_CLIENTE));

        final var faker = new Faker();
        final var nome = faker.name().fullName();
        final var prefixo = UUID.randomUUID().toString().substring(0, 8);

        return Usuario
            .builder()
            .nome(nome)
            .email(prefixo + "_" + faker.internet().emailAddress())
            .descricao(faker.yoda().quote())
            .userUrl(SlugUtils.criarSlug(nome + "-" + prefixo))
            .permissao(permissions)
            .senha("mock-password")
            .build();
    }
}
