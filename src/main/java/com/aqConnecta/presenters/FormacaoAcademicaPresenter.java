package com.aqConnecta.presenters;

import com.aqConnecta.model.FormacaoAcademica;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;


public record FormacaoAcademicaPresenter(
    @NotNull String id,
    @NotNull UniversidadePreviewPresenter universidade,
    @NotNull String descricao,
    String diploma,
    @NotNull Instant dataInicio,
    Instant dataFim,
    @NotNull boolean corrente
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
