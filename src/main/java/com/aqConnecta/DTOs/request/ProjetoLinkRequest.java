package com.aqConnecta.DTOs.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjetoLinkRequest {
    @Size(max = 150, message = "Título do link deve ter no máximo 150 caracteres")
    private String titulo;

    @Size(max = 500, message = "URL deve ter no máximo 500 caracteres")
    private String url;
}
