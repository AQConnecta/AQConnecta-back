package com.aqConnecta.DTOs.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjetoImagemResponse {
    private UUID id;
    private String url;
    private int ordem;
}
