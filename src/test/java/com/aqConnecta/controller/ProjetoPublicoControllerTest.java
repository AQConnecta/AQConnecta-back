package com.aqConnecta.controller;

import com.aqConnecta.service.DocumentoService;
import com.aqConnecta.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjetoPublicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;
    @MockBean
    private DocumentoService documentoService;

    @Container
    @ServiceConnection
    final static private MariaDBContainer<?> databaseContainer = new MariaDBContainer<>("mariadb:10.10");

    @Test
    @DisplayName("Visitante anônimo recebe 404 ao localizar projeto inexistente, sem erro 500")
    void localizarProjetoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/projeto/localizar/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Visitante anônimo recebe 404 ao pedir mídia de projeto inexistente, sem erro 500")
    void midiaProjetoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/projeto/{id}/midia/{arquivo}", UUID.randomUUID(), "capa.png"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Visitante anônimo recebe 404 ao listar posts de projeto inexistente, sem erro 500")
    void listarPostsSemAutenticacao() throws Exception {
        mockMvc.perform(get("/projeto/{id}/posts", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Visitante anônimo recebe 404 ao listar comentários de projeto inexistente, sem erro 500")
    void listarComentariosSemAutenticacao() throws Exception {
        mockMvc.perform(get("/projeto/{id}/posts/{idPost}/comentarios", UUID.randomUUID(), UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }
}
