package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.Universidade;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormacaoAcademicaRequest {

    private UUID id;
    private Universidade universidade;
    private String descricao;
    private String diploma;
    private Instant dataInicio;
    private Instant dataFim;
    private boolean atualFormacao;
}
