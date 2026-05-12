package com.aqConnecta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_USUARIO", uniqueConstraints = {
    @UniqueConstraint(name = "tb_usuario_user_url_unique_c", columnNames = {"USER_URL"}),
    @UniqueConstraint(name = "uc_tb_usuario_email", columnNames = {"EMAIL"}),

})
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

    @Column(name = "USER_URL", unique = true)
    private String userUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "RL_USUARIO_PERMISSAO",
        joinColumns = @JoinColumn(name = "ID_USUARIO", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name = "ID_PERMISSAO", referencedColumnName = "ID")
    )
    @Builder.Default
    private Set<Permissao> permissao = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "RL_USUARIO_COMPETENCIA",
        joinColumns = @JoinColumn(name = "ID_USUARIO", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name = "ID_COMPETENCIA", referencedColumnName = "ID")
    )
    @Builder.Default
    private Set<Competencia> competencias = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "usuario", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Endereco> enderecos = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "usuario", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Experiencia> experiencias = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "usuario", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<FormacaoAcademica> formacoesAcademicas = new HashSet<>();

    @Builder.Default
    @Column(name = "DELETADO")
    private Boolean deletado = false;

    @Builder.Default
    @Column(name = "ATIVADO")
    private Boolean ativado = false;

    @Column(name = "FOTO_PERFIL")
    private String fotoPerfil;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "usuario", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Curriculo> curriculo = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    private Set<Candidatura> candidaturas = new HashSet<>();

    @Column(name = "TELEFONE", nullable = true, unique = false, length = 20)
    private String telefone;

    @Column(name = "CURRICULO_LATTES", nullable = true, unique = false)
    private URI curriculoLattes;

    @Column(name = "PERFIL_GITHUB", nullable = true, unique = false)
    private URI perfilGitHub;

    @Column(name = "PERFIL_LINKEDIN", nullable = true, unique = false)
    private URI perfilLinkedin;

    public boolean ehAdministrador() {
        return this
            .getPermissao()
            .stream()
            .anyMatch(permissao -> permissao.getDescricao().equals(Permissao.ROLE_ADMIN));
    }

    public boolean verificarUsuarioNaoEAdministrador() {
        return !this.ehAdministrador();
    }

    public boolean equals(Usuario outro) {
        return outro.getId().equals(this.getId());
    }
}