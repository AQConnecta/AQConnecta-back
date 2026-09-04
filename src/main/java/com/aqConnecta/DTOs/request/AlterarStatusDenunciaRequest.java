package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.enums.StatusDenuncia;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlterarStatusDenunciaRequest {

    @NotNull(message = "Status é obrigatório")
    private StatusDenuncia status;
}
