package com.aqConnecta.presenters;

import com.aqConnecta.model.Universidade;
import jakarta.validation.constraints.NotNull;

public record UniversidadePreviewPresenter(
    @NotNull String id,
    @NotNull int codigoIes,
    @NotNull String nomeInstituicao,
    String sigla
) {
    public static UniversidadePreviewPresenter apresentar(Universidade universidade) {
        return new UniversidadePreviewPresenter(

            universidade.getId().toString(),
            universidade.getCodigoIes(),
            universidade.getNomeInstituicao(),
            universidade.getSigla()
        );
    }
}
