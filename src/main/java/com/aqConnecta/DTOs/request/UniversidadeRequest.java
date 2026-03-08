package com.aqConnecta.DTOs.request;

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
    private String nomeInstituicao;
    private String sigla;
    private String categoriaIes;
    private String organizacaoAcademica;
    private String codigoMunicipioIbge;
    private String municipio;
    private String uf;
    private String situacaoIes;
}
