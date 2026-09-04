package com.aqConnecta.model;

import com.aqConnecta.model.enums.PapelProjeto;
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
@Table(name = "TB_PROJETO_MEMBRO")
@Entity
@ToString
public class ProjetoMembro implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "ID_PROJETO")
    private Projeto projeto;

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAPEL")
    private PapelProjeto papel;

    @Builder.Default
    @Column(name = "ATIVO")
    private Boolean ativo = true;

    @Column(name = "DATA_ENTRADA")
    private LocalDateTime dataEntrada;

    @Column(name = "DATA_SAIDA")
    private LocalDateTime dataSaida;
}
