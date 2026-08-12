package com.aqConnecta.presenters;

import com.aqConnecta.model.Experiencia;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;


public record ExperienciaPresenter(
    @NotNull String id,
    @NotNull String titulo,
    @NotNull String instituicao,
    @NotNull String descricao,
    @NotNull Instant dataInicio,
    Instant dataFim,
    @NotNull boolean corrente
) {
    public static ExperienciaPresenter apresentar(Experiencia experiencia) {
        return new ExperienciaPresenter(
            experiencia.getId().toString(),
            experiencia.getTitulo(),
            experiencia.getInstituicao(),
            experiencia.getDescricao(),
            experiencia.getDataInicio(),
            experiencia.getDataFim(),
            experiencia.isAtualExperiencia()
        );
    }
}
