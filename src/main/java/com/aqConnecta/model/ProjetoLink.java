package com.aqConnecta.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_PROJETO_LINK")
@Entity
@ToString
public class ProjetoLink implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "ID_PROJETO")
    private Projeto projeto;

    @Column(name = "TITULO")
    private String titulo;

    @Column(name = "URL")
    private String url;

    @Column(name = "ORDEM")
    private int ordem;
}
