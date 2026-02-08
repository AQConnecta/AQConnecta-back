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
@Builder(toBuilder = true)
public class VagaRequest {
    @NotBlank(message = "A vaga precisa de um título.")
    private String titulo;

    @NotBlank(message = "A vaga precisa de uma descrição.")
    @Size(max = 100, message = "A descrição não pode ultrapassar {max} caracteres.")
    private String descricao;

    @NotBlank(message = "Insira o local da vaga.")
    private String localDaVaga;

    @NotNull
    private boolean aceitaRemoto;

    @Future
    private LocalDateTime dataLimiteCandidatura;

    @NotNull
    private boolean isIniciante;
}
