package com.aqConnecta.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_EXPERIENCIA")
@Entity
@ToString
public class Experiencia implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO")
    @JsonBackReference // evitar recursao infinita
    private Usuario usuario;

    @Column(name = "TITULO")
    private String titulo;
    @Column(name = "INSTITUICAO")
    private String instituicao;
    @Column(name = "DESCRICAO")
    private String descricao;
    @Column(name = "DATA_INICIO")
    private LocalDateTime dataInicio;

    // pode ser nulo
    @Column(name = "DATA_FIM")
    private LocalDateTime dataFim;
    @Column(name = "ATUAL_EXPERIENCIA")
    private boolean atualExperiencia;

}