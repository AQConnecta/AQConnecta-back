package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.enums.MotivoDenuncia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DenunciaRequest {

    @NotNull(message = "Motivo é obrigatório")
    private MotivoDenuncia motivo;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;
}
