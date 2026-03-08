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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_UNIVERSIDADE")
@Entity
@ToString
public class Universidade implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private UUID id;
    @Column(name = "CODIGO_DA_IES")
    private int codigoIes;
    @Column(name = "NOME_DA_IES")
    private String nomeInstituicao;
    @Column(name = "SIGLA")
    private String sigla;
    @Column(name = "CATEGORIA_DA_IES")
    private String categoriaIes;
    @Column(name = "ORGANIZACAO_ACADEMICA")
    private String organizacaoAcademica;
    @Column(name = "CODIGO_MUNICIPIO_IBGE")
    private String codigoMunicipioIbge;
    @Column(name = "MUNICIPIO")
    private String municipio;
    @Column(name = "UF")
    private String uf;
    @Column(name = "SITUACAO_IES")
    private String situacaoIes;

    @Builder.Default
    @ToString.Exclude
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "universidade", orphanRemoval = true)
    private Set<FormacaoAcademica> formacoesAcademicasDeUsuarios = new HashSet<>();
}