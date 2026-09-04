package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.Competencia;
import com.aqConnecta.model.Vaga;
import com.aqConnecta.model.enums.AreaAtuacao;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VagaResponse {
    private UUID id;
    private PublicadorResponse publicador;
    private String titulo;
    private String descricao;
    private String localDaVaga;
    private boolean aceitaRemoto;
    private LocalDateTime dataLimiteCandidatura;
    private Set<Competencia> competencias = new HashSet<>();
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private boolean isIniciante = false;
    private UUID projetoId;
    private String projetoTitulo;
    private AreaAtuacao areaAtuacao;

    public void inToOut(Vaga vaga) {
        this.id = vaga.getId();
        PublicadorResponse publicadorResponse = new PublicadorResponse();
        publicadorResponse.inToOut(vaga.getPublicador());
        this.publicador = publicadorResponse;
        this.titulo = vaga.getTitulo();
        this.descricao = vaga.getDescricao();
        this.localDaVaga = vaga.getLocalDaVaga();
        this.aceitaRemoto = vaga.isAceitaRemoto();
        this.dataLimiteCandidatura = vaga.getDataLimiteCandidatura();
        this.competencias = vaga.getCompetencias();
        this.criadoEm = vaga.getCriadoEm();
        this.atualizadoEm = vaga.getAtualizadoEm();
        this.isIniciante = vaga.isIniciante();
        this.areaAtuacao = vaga.getAreaAtuacao();
        if (vaga.getProjeto() != null) {
            this.projetoId = vaga.getProjeto().getId();
            this.projetoTitulo = vaga.getProjeto().getTitulo();
        }
    }
}
