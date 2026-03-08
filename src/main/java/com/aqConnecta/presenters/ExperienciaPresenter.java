package com.aqConnecta.presenters;

import com.aqConnecta.model.Experiencia;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;

public record ExperienciaPresenter(
    String id,
    String titulo,
    String instituicao,
    String descricao,
    LocalDateTime dataInicio,
    @Nullable LocalDateTime dataFim,
    boolean corrente
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
