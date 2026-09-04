package com.aqConnecta.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExperienciaRequest {

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    @NotBlank(message = "Instituição é obrigatória")
    private String instituicao;

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;
    private boolean atualExperiencia;
}
