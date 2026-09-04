package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.enums.StatusPostagem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostagemRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    private String titulo;

    @NotBlank(message = "Conteúdo é obrigatório")
    private String corpo;

    private StatusPostagem status;
}
