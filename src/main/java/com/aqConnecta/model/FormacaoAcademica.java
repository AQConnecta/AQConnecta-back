package com.aqConnecta.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_FORMACAO_ACADEMICA")
@Entity
@ToString
public class FormacaoAcademica implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    // TODO: fazer essa coluna ser non nullable. Hoje, com ela podendo ser nula, o sistema pode começar
    // a abrigar registros orfãos (adicionar optional = false no @ManyToOne e nullable = false no @JoinColumn)
    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    @JsonBackReference
    private Usuario usuario;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ID_UNIVERSIDADE", nullable = false)
    private Universidade universidade;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Column(name = "DIPLOMA")
    private String diploma;

    @Column(name = "DATA_INICIO")
    private Instant dataInicio;

    @Column(name = "DATA_FIM")
    private Instant dataFim;

    // TODO: fazer este campo ser non nullable e mudar o tipo de volta para `boolean` (primitivo)
    // como atualmente é nullable no DB, o tipo primitivo poderia ocasionar um `NullPointerException`
    // totalmente inesperado
    @Column(name = "ATUAL_FORMACAO", nullable = false)
    private Boolean atualFormacao = false;

}