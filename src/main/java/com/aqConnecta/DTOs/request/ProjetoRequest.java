package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.enums.StatusProjeto;
import com.aqConnecta.model.enums.VisibilidadeProjeto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjetoRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
    private String titulo;

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @NotNull(message = "Área é obrigatória")
    private UUID idArea;

    private StatusProjeto status;

    private VisibilidadeProjeto visibilidade;

    private UUID idUniversidade;

    @Builder.Default
    private List<ProjetoLinkRequest> links = new ArrayList<>();
}
