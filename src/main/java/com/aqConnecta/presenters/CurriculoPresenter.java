package com.aqConnecta.presenters;

import com.aqConnecta.model.Curriculo;
import jakarta.annotation.Nullable;

public record CurriculoPresenter(
    Integer id,
    String curriculo,
    @Nullable String nomeCurriculo
) {

    public static CurriculoPresenter apresentar(Curriculo curriculo) {
        return new CurriculoPresenter(
            curriculo.getId(),
            curriculo.getCurriculo(),
            curriculo.getNomeCurriculo()
        );
    }
}
