package com.aqConnecta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder(toBuilder = true)
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
//	@JsonManagedReference
    private Set<Competencia> competencias = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "usuario")
//	@JsonManagedReference // evitar recursao infinita
    private Set<Endereco> enderecos = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "usuario")
//	@JsonManagedReference // evitar recursao infinita
    private Set<Experiencia> experiencias = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "usuario")
//	@JsonManagedReference // evitar recursao infinita
    private Set<FormacaoAcademica> formacoesAcademicas = new HashSet<>();

    @Builder.Default
    @Column(name = "DELETADO")
    private Boolean deletado = false;

    @Builder.Default
    @Column(name = "ATIVADO")
    private Boolean ativado = false;

    @Column(name = "FOTO_PERFIL")
    private String fotoPerfil;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "usuario")
//	@JsonManagedReference // evitar recursão infinita
    private Set<Curriculo> curriculo = new HashSet<>();

//	@OneToMany(mappedBy = "usuario", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
//	private Set<Candidatura> candidaturas = new HashSet<>();

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