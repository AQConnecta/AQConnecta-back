package com.aqConnecta.presenters;

import com.aqConnecta.model.Competencia;
import jakarta.validation.constraints.NotNull;

public record CompetenciaPresenter(
    @NotNull String id,
    String descricao
) {
    public static CompetenciaPresenter apresentar(Competencia competencia) {
        return new CompetenciaPresenter(competencia.getId().toString(), competencia.getDescricao());
    }
}
