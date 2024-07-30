package com.aqConnecta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.context.annotation.Description;

import java.io.Serializable;
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
public class Vaga implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	@Column(name = "ID")
	private UUID id;
	
	@Column(name = "EMAIL", unique = true)
	private String email;
	
	@Column(name = "NOME")
	private String nome;
	
	@Column(name = "SENHA")
	private String senha;

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
	@JsonManagedReference // evitar recursao infinita
	private Set<Endereco> enderecos = new HashSet<>();

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "usuario")
	@JsonManagedReference // evitar recursao infinita
	private Set<Experiencia> experiencias = new HashSet<>();

	@Builder.Default
	@Column(name = "DELETADO")
    private Boolean deletado = false;

    @Builder.Default
	@Column(name = "ATIVADO")
    private Boolean ativado = false;

	@JsonIgnore
	@Description("So utilizar esse metodo na hora de retornar o usuario para o front, nao é necessario retornar a senha")
	public Vaga getUsuarioSemSenha() {
		setSenha(null);
		return this;
	}

}