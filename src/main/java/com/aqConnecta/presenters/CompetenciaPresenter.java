package com.aqConnecta.presenters;

import com.aqConnecta.model.Competencia;
import jakarta.annotation.Nullable;

public record CompetenciaPresenter(
    String id,
    @Nullable String descricao
) {
    public static CompetenciaPresenter apresentar(Competencia competencia) {
        return new CompetenciaPresenter(competencia.getId().toString(), competencia.getDescricao());
    }
}
