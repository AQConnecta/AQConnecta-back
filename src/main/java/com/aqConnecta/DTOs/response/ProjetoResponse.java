package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.Area;
import com.aqConnecta.model.Projeto;
import com.aqConnecta.model.Universidade;
import com.aqConnecta.model.enums.PapelProjeto;
import com.aqConnecta.model.enums.StatusProjeto;
import com.aqConnecta.model.enums.VisibilidadeProjeto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjetoResponse {
    private UUID id;
    private String titulo;
    private String descricao;
    private Area area;
    private Universidade universidade;
    private StatusProjeto status;
    private VisibilidadeProjeto visibilidade;
    private String capaUrl;
    private PublicadorResponse dono;
    private PapelProjeto papelUsuarioAtual;

    @Builder.Default
    private List<ProjetoImagemResponse> imagens = new ArrayList<>();

    @Builder.Default
    private List<ProjetoLinkResponse> links = new ArrayList<>();

    private long totalMembros;
    private long totalSeguidores;
    private boolean seguindo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public void inToOut(Projeto projeto) {
        this.id = projeto.getId();
        this.titulo = projeto.getTitulo();
        this.descricao = projeto.getDescricao();
        this.area = projeto.getArea();
        this.universidade = projeto.getUniversidade();
        this.status = projeto.getStatus();
        this.visibilidade = projeto.getVisibilidade();
        if (projeto.getDono() != null) {
            PublicadorResponse donoResponse = new PublicadorResponse();
            donoResponse.inToOut(projeto.getDono());
            this.dono = donoResponse;
        }
        this.criadoEm = projeto.getCriadoEm();
        this.atualizadoEm = projeto.getAtualizadoEm();
    }
}
