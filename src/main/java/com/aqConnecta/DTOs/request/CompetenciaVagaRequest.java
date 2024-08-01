package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.Competencia;
import io.jsonwebtoken.lang.Collections;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetenciaVagaRequest {
    UUID idVaga;
    Set<Competencia> competencias;

    public boolean validarDadosObrigatorios() {
        return Collections.isEmpty(competencias);
    }
}
