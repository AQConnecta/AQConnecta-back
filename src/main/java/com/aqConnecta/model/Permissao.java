package com.aqConnecta.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_PERMISSAO")
@Entity
@ToString
public class Permissao implements Serializable {
    public static final String ROLE_CLIENTE = "CLIENTE";
    public static final String ROLE_ADMIN = "ADMIN";

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "DESCRICAO")
    private String descricao;
}