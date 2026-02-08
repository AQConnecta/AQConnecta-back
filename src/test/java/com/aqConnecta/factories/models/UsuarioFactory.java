package com.aqConnecta.factories.models;

import com.aqConnecta.model.Usuario;
import net.datafaker.Faker;

public class UsuarioFactory {
    public static Usuario criar() {
        final var faker = new Faker();
        return Usuario
            .builder()
            .nome(faker.name().fullName())
            .email(faker.internet().emailAddress())
            .descricao(faker.yoda().quote())
            .userUrl(faker.internet().url())
            .senha("mock-password")
            .build();
    }
}
