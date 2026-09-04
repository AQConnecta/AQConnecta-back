package com.aqConnecta.model;

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
@Table(name = "TB_COMENTARIO")
@Entity
@ToString
@Where(clause = "DELETADO_EM IS NULL")
public class Comentario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "ID_POSTAGEM")
    private Postagem postagem;

    @ManyToOne
    @JoinColumn(name = "ID_AUTOR")
    private Usuario autor;

    @ManyToOne
    @JoinColumn(name = "ID_COMENTARIO_PAI")
    private Comentario comentarioPai;

    @Column(name = "CORPO", columnDefinition = "TEXT")
    private String corpo;

    @Column(name = "CRIADO_EM")
    private LocalDateTime criadoEm;

    @Column(name = "DELETADO_EM")
    private LocalDateTime deletadoEm;
}
