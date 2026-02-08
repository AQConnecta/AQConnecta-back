package com.aqConnecta.factories.models;

import com.aqConnecta.model.Candidatura;
import com.aqConnecta.model.Curriculo;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.Vaga;

public class CandidaturaFactory {
    public static Candidatura criar(Usuario usuario, Vaga vaga) {
        return criar(usuario, vaga, CurriculoFactory.criar(usuario));
    }

    public static Candidatura criar(Usuario usuario, Vaga vaga, Curriculo curriculo) {
        return Candidatura
            .builder()
            .vaga(vaga)
            .usuario(usuario)
            .curriculo(curriculo.getId())
            .curriculoUrl(curriculo.getCurriculo())
            .build();
    }
}
