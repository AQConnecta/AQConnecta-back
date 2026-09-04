package com.aqConnecta.DTOs.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjetoLinkResponse {
    private UUID id;
    private String titulo;
    private String url;
}
