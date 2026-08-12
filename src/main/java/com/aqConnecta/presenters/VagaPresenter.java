package com.aqConnecta.presenters;

import com.aqConnecta.model.Competencia;
import com.aqConnecta.model.Vaga;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;

public record VagaPresenter(
    @NotNull String id,
    UsuarioAutorPresenter publicador,
    @NotNull String titulo,
    @NotNull String descricao,
    @NotNull String localDaVaga,
    @NotNull boolean aceitaRemoto,
    Instant dataLimiteCandidatura,
    Set<Competencia> competencias,
    Instant criadoEm,
    Instant atualizadoEm,
    @NotNull boolean isIniciante
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
