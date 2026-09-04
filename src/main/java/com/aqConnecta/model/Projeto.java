package com.aqConnecta.model;

import com.aqConnecta.model.enums.StatusProjeto;
import com.aqConnecta.model.enums.VisibilidadeProjeto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_PROJETO")
@Entity
@ToString
@Where(clause = "DELETADO_EM IS NULL")
public class Projeto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @Column(name = "TITULO")
    private String titulo;

    @Column(name = "DESCRICAO", columnDefinition = "TEXT")
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "ID_AREA")
    private Area area;

    @ManyToOne
    @JoinColumn(name = "ID_UNIVERSIDADE")
    private Universidade universidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private StatusProjeto status;

    @Enumerated(EnumType.STRING)
    @Column(name = "VISIBILIDADE")
    private VisibilidadeProjeto visibilidade;

    @Column(name = "IMAGEM_CAPA")
    private String imagemCapa;

    @ManyToOne
    @JoinColumn(name = "ID_DONO")
    private Usuario dono;

    @Column(name = "CRIADO_EM")
    private LocalDateTime criadoEm;

    @Column(name = "ATUALIZADO_EM")
    private LocalDateTime atualizadoEm;

    @Column(name = "DELETADO_EM")
    private LocalDateTime deletadoEm;
}
