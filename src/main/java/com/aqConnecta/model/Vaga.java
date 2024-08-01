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
@Table(name = "TB_VAGA")
@Entity
@ToString
public class Vaga implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO")
    @JsonBackReference // evitar recursao infinita
    private Usuario publicador;

    @Column(name = "TITULO")
    private String titulo;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Column(name = "LOCAL_VAGA")
    private String localDaVaga;
    @Column(name = "ACEITA_REMOTO")
    private boolean aceitaRemoto;
    @Column(name = "DATA_LIMITE")
    private LocalDateTime dataLimiteCandidatura;
    @Column(name = "CRIADO_EM")
    private LocalDateTime criadoEm;
    @Column(name = "ATUALIZADO_EM")
    private LocalDateTime atualizadoEm;
    @Column(name = "DELETADO_EM")
    private LocalDateTime deletadoEm;

    // TODO: Lista de competencias
    // TODO: Lista de usuarios que se candidataram


}