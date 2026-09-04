package com.aqConnecta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_USUARIO")
@Entity
@ToString
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @Column(name = "EMAIL", unique = true)
    private String email;

    @Column(name = "NOME")
    private String nome;

    @Column(name = "DESCRICAO")
    private String descricao;

    @JsonIgnore
    @Column(name = "SENHA")
    private String senha;

    @Column(name = "USER_URL")
    private String userUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "RL_USUARIO_PERMISSAO",
        joinColumns = @JoinColumn(name = "ID_USUARIO", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name = "ID_PERMISSAO", referencedColumnName = "ID")
    )
    private Set<Permissao> permissao = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "RL_USUARIO_COMPETENCIA",
        joinColumns = @JoinColumn(name = "ID_USUARIO", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name = "ID_COMPETENCIA", referencedColumnName = "ID")
    )
    private Set<Competencia> competencias = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "usuario")
    private Set<Endereco> enderecos = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "usuario")
    private Set<Experiencia> experiencias = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "usuario")
    private Set<FormacaoAcademica> formacoesAcademicas = new HashSet<>();

    @Builder.Default
    @Column(name = "DELETADO")
    private Boolean deletado = false;

    @Column(name = "DELETADO_EM")
    private LocalDateTime deletadoEm;

    @Builder.Default
    @Column(name = "ATIVADO")
    private Boolean ativado = false;

    @Column(name = "FOTO_PERFIL")
    private String fotoPerfil;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "usuario")
    private Set<Curriculo> curriculo = new HashSet<>();

    public boolean verificarUsuarioNaoEAdministrador() {
        return this
            .getPermissao()
            .stream()
            .noneMatch(permissao -> permissao
                .getDescricao()
                .equals(Permissao.ROLE_ADMIN));
    }

    public boolean isDeleted() {
        return (deletadoEm != null) || (deletado != null && deletado);
    }
}
