package com.aqConnecta.factories.models;

import com.aqConnecta.model.Experiencia;
import com.aqConnecta.model.Usuario;
import net.datafaker.Faker;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class ExperienciaFactory {
    public static Experiencia criar(Usuario usuario) {
        final var faker = new Faker();
        return Experiencia
            .builder()
            .titulo(faker.job().title())
            .descricao(faker.job().keySkills())
            .instituicao(faker.company().name())
            .atualExperiencia(faker.bool().bool())
            .dataInicio(LocalDateTime.ofInstant(
                faker.timeAndDate().past(365 * 3, TimeUnit.DAYS),
                ZoneId.systemDefault()))
            .dataFim(faker.bool().bool()
                ? LocalDateTime.ofInstant(faker.timeAndDate().past(), ZoneId.systemDefault())
                : null)
            .usuario(usuario)
            .build();
    }
}
