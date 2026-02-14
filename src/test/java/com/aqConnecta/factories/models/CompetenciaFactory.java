package com.aqConnecta.factories.models;

import com.aqConnecta.model.Competencia;
import net.datafaker.Faker;

public class CompetenciaFactory {
    public static Competencia criar() {
        final var faker = new Faker();
        return Competencia
            .builder()
            .descricao(faker.job().keySkills())
            .build();
    }
}
