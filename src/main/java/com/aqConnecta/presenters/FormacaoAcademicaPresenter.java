package com.aqConnecta.presenters;

import com.aqConnecta.model.FormacaoAcademica;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;

public record FormacaoAcademicaPresenter(
    String id,
    UniversidadePreviewPresenter universidade,
    String descricao,
    @Nullable String diploma,
    LocalDateTime dataInicio,
    @Nullable LocalDateTime dataFim,
    boolean corrente
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
