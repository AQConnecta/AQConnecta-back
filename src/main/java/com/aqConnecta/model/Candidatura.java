package com.aqConnecta.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_CANDIDATURA")
@Entity
public class Candidatura implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "USUARIO_ID", referencedColumnName = "ID")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "VAGAS_ID", referencedColumnName = "ID")
    private Vaga vaga;

    @Column(name = "CURRICULO_ID")
    private Integer curriculo;

    @Column(name = "CURRICULO_URL")
    private String curriculoUrl;
}
