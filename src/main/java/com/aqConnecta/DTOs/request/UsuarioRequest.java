package com.aqConnecta.DTOs.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioRequest {

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;
}
