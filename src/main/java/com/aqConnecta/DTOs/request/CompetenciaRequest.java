package com.aqConnecta.DTOs.request;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetenciaRequest {
    private UUID id;
    private String descricao;
}
