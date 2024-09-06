package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeuUsuarioResponse {
    private UUID id;
    private String email;
    private String nome;
    private String descricao;
    private Set<Permissao> permissao = new HashSet<>();
    private Set<Competencia> competencias = new HashSet<>();
    private Set<Endereco> enderecos = new HashSet<>();
    private Set<Experiencia> experiencias = new HashSet<>();
    private Boolean deletado = false;
    private Boolean ativado = false;
    private String fotoPerfil;
    private Set<Curriculo> curriculo = new HashSet<>();


    public void inToOut(Usuario usuarioIn) {
        this.id = usuarioIn.getId();
        this.email = usuarioIn.getEmail();
        this.nome = usuarioIn.getNome();
        this.descricao = usuarioIn.getDescricao();
        this.permissao.addAll(usuarioIn.getPermissao());
        this.competencias.addAll(usuarioIn.getCompetencias());
        this.enderecos.addAll(usuarioIn.getEnderecos());
        this.experiencias.addAll(usuarioIn.getExperiencias());
        this.deletado = usuarioIn.getDeletado();
        this.ativado = usuarioIn.getAtivado();
        this.fotoPerfil = usuarioIn.getFotoPerfil();
        this.curriculo.addAll(usuarioIn.getCurriculo());
    }

}
