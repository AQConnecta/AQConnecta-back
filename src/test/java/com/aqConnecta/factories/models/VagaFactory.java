package com.aqConnecta.factories.models;

import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.Vaga;
import net.datafaker.Faker;

public class VagaFactory {
    public static Vaga criar(Usuario autor) {
        final var faker = new Faker();

        return Vaga.builder()
            .publicador(autor)
            .localDaVaga(faker.locality().localeString())
            .titulo(faker.job().title())
            .descricao(faker.job().keySkills())
            .aceitaRemoto(faker.bool().bool())
            .isIniciante(faker.bool().bool())
            .atualizadoEm(faker.bool().bool()
                ? faker.timeAndDate().future()
                : null)
            .build();
    }
}
