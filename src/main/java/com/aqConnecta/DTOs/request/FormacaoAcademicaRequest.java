package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.Universidade;
import com.aqConnecta.model.Usuario;
import lombok.*;

import java.time.LocalDateTime;
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
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private boolean atualFormacao;
}
