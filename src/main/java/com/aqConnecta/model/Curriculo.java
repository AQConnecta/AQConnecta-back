package com.aqConnecta.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "RL_USUARIO_CURRICULO")
@Entity
@ToString
public class Curriculo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "USUARIO_ID", referencedColumnName = "ID")
    @JsonBackReference
    private Usuario usuario;

    @Column(name = "CURRICULO")
    private String curriculo;

    @Column(name = "NOME_CURRICULO")
    private String nomeCurriculo;

}
