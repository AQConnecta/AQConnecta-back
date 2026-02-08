package com.aqConnecta;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class E2ETest {

    @Autowired
    protected Flyway flyway;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @ServiceConnection
    final static protected MariaDBContainer<?> databaseContainer = new MariaDBContainer<>("mariadb:10.10");

    static {
        databaseContainer.start();
    }

    @BeforeAll
    void setUp() {
        flyway.clean();
        flyway.migrate();
    }

    @BeforeEach
    void cleanUp() throws Exception {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        jdbcTemplate.execute("TRUNCATE TABLE RL_USUARIO_CURRICULO");
        jdbcTemplate.execute("TRUNCATE TABLE RL_USUARIO_COMPETENCIA");
        jdbcTemplate.execute("TRUNCATE TABLE RL_VAGA_COMPETENCIA");
        jdbcTemplate.execute("TRUNCATE TABLE TB_CANDIDATURA");
        jdbcTemplate.execute("TRUNCATE TABLE TB_CONFIRMA_TOKEN");
        jdbcTemplate.execute("TRUNCATE TABLE TB_ENDERECO");
        jdbcTemplate.execute("TRUNCATE TABLE TB_FORMACAO_ACADEMICA");
        jdbcTemplate.execute("TRUNCATE TABLE TB_EXPERIENCIA");
        jdbcTemplate.execute("TRUNCATE TABLE TB_VAGA");
        jdbcTemplate.execute("TRUNCATE TABLE TB_USUARIO");

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
}
