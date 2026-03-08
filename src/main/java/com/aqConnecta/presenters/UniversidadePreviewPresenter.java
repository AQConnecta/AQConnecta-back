package com.aqConnecta.presenters;

import com.aqConnecta.model.Universidade;
import jakarta.annotation.Nullable;

public record UniversidadePreviewPresenter(
    String id,
    int codigoIes,
    String nomeInstituicao,
    @Nullable String sigla
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
