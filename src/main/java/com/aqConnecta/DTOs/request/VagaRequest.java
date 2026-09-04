package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.enums.AreaAtuacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
    private String titulo;

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    private String localDaVaga;
    private boolean aceitaRemoto;
    private LocalDateTime dataLimiteCandidatura;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private boolean isIniciante;
    private UUID idProjeto;
    private AreaAtuacao areaAtuacao;
}
