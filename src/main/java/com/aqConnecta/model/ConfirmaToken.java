package com.aqConnecta.model;

import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_CONFIRMA_TOKEN")
@Entity
@ToString
public class ConfirmaToken {

    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    @Column(name = "ID")
    private UUID Id;

    @Column(name = "TOKEN")
    private String token;

    @Column(name = "DATA_CRIACAO")
    private Timestamp dataCriacao;

    @Column(name = "DATA_EXPIRACAO")
    private Timestamp dataExpiracao;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USUARIO", referencedColumnName = "ID")
    private Usuario usuario;

    public boolean isExpirado() {
        return dataExpiracao != null && dataExpiracao.before(new Timestamp(System.currentTimeMillis()));
    }
}
