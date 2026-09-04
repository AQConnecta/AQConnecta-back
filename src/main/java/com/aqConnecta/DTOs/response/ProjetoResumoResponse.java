package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.Area;
import com.aqConnecta.model.Projeto;
import com.aqConnecta.model.Universidade;
import com.aqConnecta.model.enums.StatusProjeto;
import com.aqConnecta.model.enums.VisibilidadeProjeto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjetoResumoResponse {
    private UUID id;
    private String titulo;
    private String descricao;
    private Area area;
    private Universidade universidade;
    private StatusProjeto status;
    private VisibilidadeProjeto visibilidade;
    private String capaUrl;
    private UUID donoId;
    private String donoNome;
    private LocalDateTime criadoEm;

    public void inToOut(Projeto projeto) {
        this.id = projeto.getId();
        this.titulo = projeto.getTitulo();
        this.descricao = projeto.getDescricao();
        this.area = projeto.getArea();
        this.universidade = projeto.getUniversidade();
        this.status = projeto.getStatus();
        this.visibilidade = projeto.getVisibilidade();
        if (projeto.getDono() != null) {
            this.donoId = projeto.getDono().getId();
            this.donoNome = projeto.getDono().getNome();
        }
        this.criadoEm = projeto.getCriadoEm();
    }
}
