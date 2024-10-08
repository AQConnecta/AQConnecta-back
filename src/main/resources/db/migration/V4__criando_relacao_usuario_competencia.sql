CREATE TABLE IF NOT EXISTS RL_USUARIO_COMPETENCIA
(
    ID_USUARIO     BINARY(36) NOT NULL,
    ID_COMPETENCIA BINARY(36) NOT NULL,
    PRIMARY KEY (ID_USUARIO, ID_COMPETENCIA),
    CONSTRAINT fk_usuario_competencia FOREIGN KEY (ID_USUARIO) REFERENCES TB_USUARIO (ID),
    CONSTRAINT fk_competencia_usuario FOREIGN KEY (ID_COMPETENCIA) REFERENCES TB_COMPETENCIA (ID)
);