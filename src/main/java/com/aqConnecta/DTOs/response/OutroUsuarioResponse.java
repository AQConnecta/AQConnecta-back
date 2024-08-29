package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutroUsuarioResponse {
    private UUID id;
    private String email;
    private String nome;
    private String descricao;
    private Set<Competencia> competencias = new HashSet<>();
    private Set<Experiencia> experiencias = new HashSet<>();
    private Boolean deletado = false;
    private Boolean ativado = false;
    private String fotoPerfil;

    public void inToOut(Usuario usuarioIn) {
        this.id = usuarioIn.getId();
        this.email = usuarioIn.getEmail();
        this.nome = usuarioIn.getNome();
        this.descricao = usuarioIn.getDescricao();
        this.competencias = usuarioIn.getCompetencias();
        this.experiencias = usuarioIn.getExperiencias();
        this.deletado = usuarioIn.getDeletado();
        this.ativado = usuarioIn.getAtivado();
        this.fotoPerfil = usuarioIn.getFotoPerfil();
    }
}
