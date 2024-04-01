package com.aqConnecta.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.aqConnecta.model.enums.Perfil;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
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
	@Column(name = "ID", length = 19)
	private UUID id;
	
	@Column(name = "EMAIL", unique = true)
	private String email;
	
	@Column(name = "NOME")
	private String nome;
	
	@Column(name = "SENHA")
	private String senha;
	
	@Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TB_USUARIO_PERFIL", joinColumns = @JoinColumn(name = "ID_USUARIO"))
    @Column(name = "ID_PERFIL")
    private Set<Integer> perfis = new HashSet<>();
    
    @Builder.Default
    private Boolean deletado = false;

    public Set<Perfil> getPerfis() {
        return perfis.stream().map(Perfil::toEnum).collect(Collectors.toSet());
    }

    public void addPerfil(Perfil perfil) {
        this.perfis.add(perfil.getCodigo());
    }
	
}