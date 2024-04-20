package com.aqConnecta.DTOs.request;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CadastroExperienciaRequest {
    private UUID id; // pode ser nulo
    private String titulo;
    private String instituicao;
    private String descricao;
    // yyyy-MM-dd'T'HH:mm:ss
    private LocalDateTime dataInicio;
    // pode ser nulo
    private LocalDateTime dataFim;
    private boolean atualExperiencia;

    public boolean validarDadosObrigatorios() {
        return !titulo.isEmpty() && !instituicao.isEmpty() && !descricao.isEmpty() && dataInicio != null;
    }
}
