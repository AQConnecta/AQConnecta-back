package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.Competencia;
import io.jsonwebtoken.lang.Collections;
import lombok.*;
import org.apache.logging.log4j.util.Strings;

import java.util.Collection;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetenciaUsuarioRequest {
    Set<Competencia> competencias;

    public boolean validarDadosObrigatorios() {
        return Collections.isEmpty(competencias);
    }
}
