package com.aqConnecta.factories.models;

import com.aqConnecta.model.Curriculo;
import com.aqConnecta.model.Usuario;
import net.datafaker.Faker;

public class CurriculoFactory {
    public static Curriculo criar(Usuario usuario) {
        final var faker = new Faker();
        return Curriculo
            .builder()
            .curriculo(faker.internet().url())
            .nomeCurriculo(faker.job().title() + " CV")
            .usuario(usuario)
            .build();
    }
}
