package com.aqConnecta.model;

import com.aqConnecta.model.enums.PapelProjeto;
import com.aqConnecta.model.enums.StatusConvite;
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
@Table(name = "TB_PROJETO_CONVITE")
@Entity
@ToString
public class ProjetoConvite implements Serializable {
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
    private Usuario usuarioConvidado;

    @ManyToOne
    @JoinColumn(name = "ID_CONVIDADO_POR")
    private Usuario convidadoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAPEL")
    private PapelProjeto papel;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private StatusConvite status;

    @Column(name = "CRIADO_EM")
    private LocalDateTime criadoEm;

    @Column(name = "RESPONDIDO_EM")
    private LocalDateTime respondidoEm;
}
