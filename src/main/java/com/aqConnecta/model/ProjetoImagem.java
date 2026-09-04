package com.aqConnecta.model;

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
@Table(name = "TB_PROJETO_IMAGEM")
@Entity
@ToString
public class ProjetoImagem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "ID_PROJETO")
    private Projeto projeto;

    @Column(name = "CAMINHO")
    private String caminho;

    @Column(name = "ORDEM")
    private int ordem;

    @Column(name = "CRIADO_EM")
    private LocalDateTime criadoEm;
}
