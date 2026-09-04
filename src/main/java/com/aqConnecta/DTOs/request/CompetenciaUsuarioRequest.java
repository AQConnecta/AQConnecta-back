package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.Competencia;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetenciaUsuarioRequest {

    @NotEmpty(message = "Lista de competências não pode ser vazia")
    Set<Competencia> competencias;
}
