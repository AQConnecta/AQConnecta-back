package com.aqConnecta.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "TB_FORMACAO_ACADEMICA")
@Entity
@ToString
public class FormacaoAcademica implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO")
    @JsonBackReference // evitar recursao infinita
    private Usuario usuario;
    @OneToOne
    @JoinColumn(name = "ID_UNIVERSIDADE")
//    @JsonManagedReference // evitar recursao infinita
    private Universidade universidade;
    @Column(name = "DESCRICAO")
    private String descricao;
    @Column(name = "DIPLOMA")
    private String diploma;
    @Column(name = "DATA_INICIO")
    private LocalDateTime dataInicio;
    @Column(name = "DATA_FIM")
    private LocalDateTime dataFim;
    @Column(name = "ATUAL_FORMACAO")
    private boolean atualFormacao;

}