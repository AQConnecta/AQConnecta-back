package com.aqConnecta.DTOs.request;

import jakarta.validation.constraints.Future;
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
public class VagaRequest {
    @NotBlank(message = "A vaga precisa de um título.")
    private String titulo;

    @NotBlank(message = "A vaga precisa de uma descrição.")
    @Size(max = 200, message = "A descrição não pode ultrapassar 200 caracteres.")
    private String descricao;

    @NotBlank(message = "Insira o local da vaga.")
    private String localDaVaga;

    @NotNull
    private boolean aceitaRemoto;

    @NotNull
    @Future
    private LocalDateTime dataLimiteCandidatura;

    @NotNull
    private boolean isIniciante;
}
