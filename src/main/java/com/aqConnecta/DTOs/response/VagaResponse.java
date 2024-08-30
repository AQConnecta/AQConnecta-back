package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.Vaga;
import lombok.*;

import java.time.LocalDateTime;
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
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private boolean isIniciante = false;

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
        this.criadoEm = vaga.getCriadoEm();
        this.atualizadoEm = vaga.getAtualizadoEm();
        this.isIniciante = vaga.isIniciante();
    }
}
