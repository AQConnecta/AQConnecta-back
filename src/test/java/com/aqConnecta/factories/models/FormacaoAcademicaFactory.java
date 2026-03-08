package com.aqConnecta.factories.models;

import com.aqConnecta.model.FormacaoAcademica;
import com.aqConnecta.model.Universidade;
import com.aqConnecta.model.Usuario;
import net.datafaker.Faker;

import java.util.concurrent.TimeUnit;

public class FormacaoAcademicaFactory {
    public static FormacaoAcademica criar(Usuario usuario) {
        return criar(usuario, UniversidadeFactory.criar());
    }

    public static FormacaoAcademica criar(Usuario usuario, Universidade universidade) {
        final var faker = new Faker();
        final var atualFormacao = faker.bool().bool();
        return FormacaoAcademica
            .builder()
            .universidade(universidade)
            .usuario(usuario)
            .descricao(faker.educator().course())
            .dataInicio(faker.timeAndDate().past(365 * 5, TimeUnit.DAYS))
            .dataFim(atualFormacao
                ? null
                : faker.timeAndDate().past(365, TimeUnit.DAYS))
            .atualFormacao(atualFormacao)
            .diploma(faker.bool().bool() ? faker.internet().url() : null)
            .build();
    }
}
