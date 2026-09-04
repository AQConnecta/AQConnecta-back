package com.aqConnecta.model;

import com.aqConnecta.model.enums.AreaAtuacao;
import com.aqConnecta.model.enums.StatusCompetencia;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_COMPETENCIA")
@Entity
@ToString
public class Competencia implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;
    @Column(name = "DESCRICAO")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORIA")
    private AreaAtuacao categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private StatusCompetencia status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Competencia that = (Competencia) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}