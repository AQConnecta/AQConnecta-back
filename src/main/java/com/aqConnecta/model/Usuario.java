package com.aqConnecta.model;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Description;

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
	@GeneratedValue(strategy=GenerationType.UUID)
	@Column(name = "ID")
	private UUID id;
	
	@Column(name = "EMAIL", unique = true)
	private String email;
	
	@Column(name = "NOME")
	private String nome;

	@Column(name = "DESCRICAO")
	private String descricao;

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

	@JsonIgnore
	@Description("So utilizar esse metodo na hora de retornar o usuario para o front, nao é necessario retornar a senha")
	public Usuario getUsuarioSemSenha() {
		setSenha(null);
		return this;
	}

	@JsonIgnore
	public boolean verificarUsuarioNaoEAdministrador() {
		return this
				.getPermissao()
				.stream()
				.noneMatch(permissao -> permissao
						.getDescricao()
						.equals(Permissao.ROLE_ADMIN));
	}
}