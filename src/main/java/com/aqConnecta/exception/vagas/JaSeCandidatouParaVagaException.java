package com.aqConnecta.exception.vagas;

import com.aqConnecta.model.Vaga;

import java.text.MessageFormat;

public class JaSeCandidatouParaVagaException extends RuntimeException {
    public JaSeCandidatouParaVagaException(Vaga vaga) {
        super(MessageFormat.format("Você já se candidatou para a vaga \"{0}\".", vaga.getTitulo()));
    }
}
