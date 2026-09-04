package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.enums.PapelProjeto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MembroResponse {
    private UUID id;
    private PublicadorResponse usuario;
    private PapelProjeto papel;
    private Boolean ativo;
    private LocalDateTime dataEntrada;
    private LocalDateTime dataSaida;
}
