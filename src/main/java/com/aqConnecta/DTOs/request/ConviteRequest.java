package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.enums.PapelProjeto;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConviteRequest {

    private UUID idUsuario;

    private String email;

    @NotNull(message = "Papel é obrigatório")
    private PapelProjeto papel;
}
