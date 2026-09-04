package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.enums.AreaAtuacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetenciaRequest {
    private UUID id;

    @NotBlank(message = "Descrição da competência é obrigatória")
    @Size(max = 300, message = "Descrição deve ter no máximo 300 caracteres")
    private String descricao;

    private AreaAtuacao categoria;
}
