package com.aqConnecta.factories.models;

import com.aqConnecta.model.Permissao;
import com.aqConnecta.model.Usuario;
import net.datafaker.Faker;

import java.util.HashSet;

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
        return Usuario
            .builder()
            .nome(faker.name().fullName())
            .email(faker.internet().emailAddress())
            .descricao(faker.yoda().quote())
            .userUrl(faker.internet().url())
            .permissao(permissions)
            .senha("mock-password")
            .build();
    }
}
