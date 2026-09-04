package com.aqConnecta.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AreaRequest {

    @NotBlank(message = "Descrição da área é obrigatória")
    @Size(max = 100, message = "Descrição deve ter no máximo 100 caracteres")
    private String descricao;
}
