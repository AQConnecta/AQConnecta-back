package com.aqConnecta.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
	
	@Column(name = "SENHA")
	private String senha;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "RL_USUARIO_PERMISSAO",
			joinColumns = @JoinColumn(name = "ID_USUARIO", referencedColumnName = "ID"),
			inverseJoinColumns = @JoinColumn(name = "ID_PERMISSAO", referencedColumnName = "ID")
	)
	private Set<Permissao> permissao = new HashSet<>();

	@Builder.Default
	@Column(name = "DELETADO")
    private Boolean deletado = false;

    @Builder.Default
	@Column(name = "ATIVADO")
    private Boolean ativado = false;

}