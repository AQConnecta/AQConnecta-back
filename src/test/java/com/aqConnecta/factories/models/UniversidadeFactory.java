package com.aqConnecta.factories.models;

import com.aqConnecta.model.Universidade;
import net.datafaker.Faker;

public class UniversidadeFactory {
    public static Universidade criar() {
        final var faker = new Faker();

        final var uniFake = faker.university();

        return Universidade
            .builder()
            .nomeInstituicao(uniFake.name())
            .codigoIes(faker.number().positive())
            .build();
    }
}
