package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.enums.PapelProjeto;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlterarPapelRequest {

    @NotNull(message = "Papel é obrigatório")
    private PapelProjeto papel;
}
