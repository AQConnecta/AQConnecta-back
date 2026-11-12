package com.aqConnecta.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExperienciaRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
    private String titulo;

    @NotBlank(message = "Instituição é obrigatória")
    @Size(max = 150, message = "Instituição deve ter no máximo 150 caracteres")
    private String instituicao;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
    private String descricao;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;
    private boolean atualExperiencia;
}
