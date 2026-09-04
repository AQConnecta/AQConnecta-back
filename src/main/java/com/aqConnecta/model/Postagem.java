package com.aqConnecta.model;

import com.aqConnecta.model.enums.StatusPostagem;
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
@Table(name = "TB_POSTAGEM")
@Entity
@ToString
@Where(clause = "DELETADO_EM IS NULL")
public class Postagem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "ID_PROJETO")
    private Projeto projeto;

    @ManyToOne
    @JoinColumn(name = "ID_AUTOR")
    private Usuario autor;

    @Column(name = "TITULO")
    private String titulo;

    @Column(name = "CORPO", columnDefinition = "TEXT")
    private String corpo;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private StatusPostagem status;

    @Column(name = "PUBLICADO_EM")
    private LocalDateTime publicadoEm;

    @Column(name = "CRIADO_EM")
    private LocalDateTime criadoEm;

    @Column(name = "ATUALIZADO_EM")
    private LocalDateTime atualizadoEm;

    @Column(name = "DELETADO_EM")
    private LocalDateTime deletadoEm;
}
