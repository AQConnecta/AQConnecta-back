package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.Competencia;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetenciaVagaRequest {

    @NotNull(message = "ID da vaga é obrigatório")
    UUID idVaga;

    @NotEmpty(message = "Lista de competências não pode ser vazia")
    Set<Competencia> competencias;
}
