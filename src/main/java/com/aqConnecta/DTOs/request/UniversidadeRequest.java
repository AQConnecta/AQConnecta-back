package com.aqConnecta.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UniversidadeRequest {

    private UUID id;
    private int codigoIes;

    @NotBlank(message = "Nome da instituição é obrigatório")
    private String nomeInstituicao;

    private String sigla;
    private String categoriaIes;
    private String organizacaoAcademica;
    private String codigoMunicipioIbge;
    private String municipio;

    @NotBlank(message = "UF é obrigatória")
    private String uf;

    private String situacaoIes;
}
