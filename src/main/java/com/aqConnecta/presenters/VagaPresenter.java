package com.aqConnecta.presenters;

import com.aqConnecta.model.Competencia;
import com.aqConnecta.model.Vaga;

import java.time.LocalDateTime;
import java.util.Set;

public record VagaPresenter(
    String id,
    UsuarioAutorPresenter publicador,
    String titulo,
    String descricao,
    String localDaVaga,
    boolean aceitaRemoto,
    LocalDateTime dataLimiteCandidatura,
    Set<Competencia> competencias,
    LocalDateTime criadoEm,
    LocalDateTime atualizadoEm,
    boolean isIniciante
) {
    public static VagaPresenter apresentar(Vaga vaga) {
        return new VagaPresenter(
            vaga.getId().toString(),
            UsuarioAutorPresenter.apresentar(vaga.getPublicador()),
            vaga.getTitulo(),
            vaga.getDescricao(),
            vaga.getLocalDaVaga(),
            vaga.isAceitaRemoto(),
            vaga.getDataLimiteCandidatura(),
            vaga.getCompetencias(),
            vaga.getCriadoEm(),
            vaga.getAtualizadoEm(),
            vaga.isIniciante()
        );
    }
}
