package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.LoginRequest;
import com.aqConnecta.config.AWSClientConfig;
import com.aqConnecta.model.Permissao;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.repository.PermissaoRepository;
import com.aqConnecta.repository.UsuarioRepository;
import com.aqConnecta.service.DocumentoService;
import com.aqConnecta.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;

import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissaoRepository permissaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Mockamos esses para não interagirem com nada externo
    @MockBean
    private EmailService emailService;
    @MockBean
    private DocumentoService documentoService;
    @MockBean
    private AWSClientConfig awsClientConfig;

    @Container
    @ServiceConnection
    final static private MariaDBContainer<?> databaseContainer = new MariaDBContainer<>("mariadb:10.10");

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void setUp() {
        flyway.clean();
        flyway.migrate();
    }


    final private String email = "teste@mail.com";
    final private String senha = "12345678";

    private Usuario getUser() {
        final var permissoes = new HashSet<Permissao>();
        permissoes.add(this.permissaoRepository.findById(1L)
            .orElseThrow(() -> new RuntimeException("Erro interno, não foi possivel criar conta com permissão de cliente")));

        return Usuario.builder()
            .id(UUID.randomUUID())
            .nome("John Doe")
            .email(this.email)
            .senha(this.passwordEncoder.encode(this.senha))
            .permissao(permissoes)
            .build();
    }

    @Test
    @DisplayName("Não deveria permitir um usuário logar sem que seu e-mail esteja confirmado")
    void login_with_unconfirmed_email() throws Exception {
        final var usuario = this.getUser();
        this.usuarioRepository.save(usuario);

        // Credenciais corretas
        final var reqDto = LoginRequest.builder()
            .email(this.email)
            .senha(this.senha)
            .build();

        final var reqJson = this.objectMapper.writeValueAsString(reqDto);

        this.mockMvc
            .perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson)
                .with(csrf())
                .with(anonymous()))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString(email)));
    }

    @Test
    @DisplayName("Não deveria permitir um usuário se autenticar com credenciais inválidas")
    void login_with_invalid_credentials() throws Exception {
        final var usuario = this.getUser();
        // Confirma o e-mail
        usuario.setAtivado(true);
        this.usuarioRepository.save(usuario);

        // Credenciais incorretas
        final var reqDto = LoginRequest.builder()
                .email(this.email)
                .senha("123")
                .build();

        final var reqJson = this.objectMapper.writeValueAsString(reqDto);

        this.mockMvc
                .perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson)
                        .with(csrf())
                        .with(anonymous()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.not(Matchers.containsString(email))));
    }

    @Test
    @DisplayName("Deveria autenticar um usuário com credenciais corretas e email confirmado")
    void login_with_valid_credentials_and_confirmed_email() throws Exception {
        final var usuario = this.getUser();
        usuario.setAtivado(true);
        this.usuarioRepository.save(usuario);

        // Credenciais corretas
        final var reqDto = LoginRequest.builder()
                .email(this.email)
                .senha(this.senha)
                .build();

        final var reqJson = this.objectMapper.writeValueAsString(reqDto);

        this.mockMvc
                .perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson)
                        .with(csrf())
                        .with(anonymous()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.usuario").exists());
    }

    @Test
    @DisplayName("Não deveria autenticar um usuário soft-deleted")
    void login_as_disabled_user() throws Exception {
        final var usuario = this.getUser();
        usuario.setAtivado(true);
        usuario.setDeletado(true);
        this.usuarioRepository.save(usuario);

        // Credenciais corretas
        final var reqDto = LoginRequest.builder()
                .email(this.email)
                .senha(this.senha)
                .build();

        final var reqJson = this.objectMapper.writeValueAsString(reqDto);

        this.mockMvc
                .perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson)
                        .with(csrf())
                        .with(anonymous()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }
}