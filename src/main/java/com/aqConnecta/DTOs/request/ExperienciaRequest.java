package com.aqConnecta.DTOs.request;

import lombok.*;
import org.apache.logging.log4j.util.Strings;

import java.time.Instant;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExperienciaRequest {
    private String titulo;
    private String instituicao;
    private String descricao;
    // yyyy-MM-dd'T'HH:mm:ss
    private Instant dataInicio;
    // pode ser nulo
    private Instant dataFim;
    private boolean atualExperiencia;

    public boolean validarDadosObrigatorios() {
        return !Strings.isEmpty(titulo) && !Strings.isEmpty(instituicao) &&
               !Strings.isEmpty(descricao) && dataInicio != null;
    }
}
