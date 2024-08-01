CREATE TABLE IF NOT EXISTS RL_VAGA_COMPETENCIA
(
    ID_VAGA BINARY(36) NOT NULL,
    ID_COMPETENCIA BINARY(36) NOT NULL,
    PRIMARY KEY (ID_VAGA, ID_COMPETENCIA),
    CONSTRAINT fk_vaga_competencia FOREIGN KEY (ID_VAGA) REFERENCES TB_VAGA (ID),
    CONSTRAINT fk_competencia_vaga FOREIGN KEY (ID_COMPETENCIA) REFERENCES TB_COMPETENCIA (ID)
    );