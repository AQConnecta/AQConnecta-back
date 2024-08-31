package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.Permissao;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.Vaga;
import jakarta.persistence.Column;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicadorResponse {
    private UUID id;
    private String email;
    private String nome;
    private Set<Permissao> permissao = new HashSet<>();
    private Boolean deletado = false;
    private Boolean ativado = false;
    private String fotoPerfil;
    private String userUrl;

    public void inToOut(Usuario publicadorIn) {
        this.id = publicadorIn.getId();
        this.email = publicadorIn.getEmail();
        this.nome = publicadorIn.getNome();
        this.permissao.addAll(publicadorIn.getPermissao());
        this.deletado = publicadorIn.getDeletado();
        this.ativado = publicadorIn.getAtivado();
        this.fotoPerfil = publicadorIn.getFotoPerfil();
        this.userUrl = publicadorIn.getUserUrl();
    }
}
