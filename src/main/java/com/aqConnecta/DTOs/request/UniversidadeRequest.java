package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.Universidade;
import com.aqConnecta.model.Usuario;
import lombok.*;

import java.time.LocalDateTime;
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
