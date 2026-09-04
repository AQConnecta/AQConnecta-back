package com.aqConnecta.model;

import com.aqConnecta.model.enums.MotivoDenuncia;
import com.aqConnecta.model.enums.StatusDenuncia;
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
@Table(name = "TB_DENUNCIA")
@Entity
@ToString
public class Denuncia implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "ID_PROJETO")
    private Projeto projeto;

    @ManyToOne
    @JoinColumn(name = "ID_DENUNCIANTE")
    private Usuario denunciante;

    @Enumerated(EnumType.STRING)
    @Column(name = "MOTIVO")
    private MotivoDenuncia motivo;

    @Column(name = "DESCRICAO", columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private StatusDenuncia status;

    @ManyToOne
    @JoinColumn(name = "ID_RESOLVIDO_POR")
    private Usuario resolvidoPor;

    @Column(name = "CRIADO_EM")
    private LocalDateTime criadoEm;

    @Column(name = "RESOLVIDO_EM")
    private LocalDateTime resolvidoEm;
}
