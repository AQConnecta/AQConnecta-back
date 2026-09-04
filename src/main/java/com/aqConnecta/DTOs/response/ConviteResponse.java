package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.enums.PapelProjeto;
import com.aqConnecta.model.enums.StatusConvite;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConviteResponse {
    private UUID id;
    private UUID projetoId;
    private String projetoTitulo;
    private String projetoCapaUrl;
    private PublicadorResponse usuario;
    private String convidadoPorNome;
    private PapelProjeto papel;
    private StatusConvite status;
    private LocalDateTime criadoEm;
}
