package com.aqConnecta.presenters;

import com.aqConnecta.model.FormacaoAcademica;
import jakarta.annotation.Nullable;

import java.time.Instant;


public record FormacaoAcademicaPresenter(
    String id,
    UniversidadePreviewPresenter universidade,
    String descricao,
    @Nullable String diploma,
    boolean corrente
    @NotNull Instant dataInicio,
    Instant dataFim,
) {
    public static FormacaoAcademicaPresenter apresentar(FormacaoAcademica formacao) {
        final var corrente = formacao.getAtualFormacao() != null && formacao.getAtualFormacao();

        return new FormacaoAcademicaPresenter(
            formacao.getId().toString(),
            UniversidadePreviewPresenter.apresentar(formacao.getUniversidade()),
            formacao.getDescricao(),
            formacao.getDiploma(),
            formacao.getDataInicio(),
            formacao.getDataFim(),
            corrente
        );
    }
}
