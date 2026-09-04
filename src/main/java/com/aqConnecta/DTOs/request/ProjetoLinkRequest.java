package com.aqConnecta.DTOs.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjetoLinkRequest {
    private String titulo;
    private String url;
}
