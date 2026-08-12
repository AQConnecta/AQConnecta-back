package com.aqConnecta.presenters;

import com.aqConnecta.model.Curriculo;
import jakarta.validation.constraints.NotNull;

public record CurriculoPresenter(
    @NotNull Integer id,
    @NotNull String curriculo,
    String nomeCurriculo
) {

    public static CurriculoPresenter apresentar(Curriculo curriculo) {
        return new CurriculoPresenter(
            curriculo.getId(),
            curriculo.getCurriculo(),
            curriculo.getNomeCurriculo()
        );
    }
}
