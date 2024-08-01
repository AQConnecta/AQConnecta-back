package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.Usuario;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VagaRequest {
    private UUID id;
    private Usuario publicador;
    private String titulo;
    private String descricao;
    private String localDaVaga;
    private boolean aceitaRemoto;
    private LocalDateTime dataLimiteCandidatura;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private LocalDateTime deletadoEm;
}
