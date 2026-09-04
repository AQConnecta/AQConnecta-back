package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.Universidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormacaoAcademicaRequest {

    private UUID id;

    @NotNull(message = "Universidade é obrigatória")
    private Universidade universidade;

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    /** Opcional: nome/tipo do diploma. Pode ser preenchido depois. */
    private String diploma;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;
    private boolean atualFormacao;
}
