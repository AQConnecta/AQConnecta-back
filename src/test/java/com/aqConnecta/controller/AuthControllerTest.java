package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.LoginRequest;
import com.aqConnecta.config.AWSClientConfig;
import com.aqConnecta.model.Permissao;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.repository.PermissaoRepository;
import com.aqConnecta.repository.UsuarioRepository;
import com.aqConnecta.security.JWTUtil;
import com.aqConnecta.service.AuthService;
import com.aqConnecta.service.DocumentoService;
import com.aqConnecta.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private AuthService authService;

    // Mockamos esses para não interagirem com nada externo
    @MockBean
    private EmailService emailService;
    @MockBean
    private DocumentoService documentoService;
    @MockBean
    private AWSClientConfig awsClientConfig;

    @Container
    @ServiceConnection
    final static private MariaDBContainer<?>
        databaseContainer =
        new MariaDBContainer<>("mariadb:10.10");

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
            .orElseThrow(() -> new RuntimeException(
                "Erro interno, não foi possivel criar conta com permissão de cliente")));

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
            .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                Matchers.not(Matchers.containsString(email))));
    }

    @Test
    @DisplayName("Deveria autenticar um usuário com credenciais corretas e email confirmado")
    void loginWithValidCredentialsAndConfirmedEmail() throws Exception {
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
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.token").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.usuario").exists());
    }

    @Test
    @DisplayName("Não deveria autenticar um usuário soft-deleted")
    void loginAsDisabledUser() throws Exception {
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
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Não deveria retornar a senha do usuário")
    void noPasswordInLoginUserResponse() throws Exception {
        final var usuario = this.getUser();
        usuario.setAtivado(true);
        this.usuarioRepository.save(usuario);

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
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.usuario.senha").doesNotExist());
    }

    @Test
    @DisplayName("Deveria reautenticar um usuário com um refresh token válido")
    void reauthenticatingValidRefreshToken() throws Exception {
        var usuario = this.getUser();
        usuario.setAtivado(true);
        usuario = this.usuarioRepository.save(usuario);

        final var validRefreshToken = this.jwtUtil.generateRefreshToken(usuario.getId(), usuario.getEmail());
        final var requestCookie = this.authService.obterRefreshCookie(validRefreshToken);

        var result = this.mockMvc
            .perform(post("/auth/refresh")
                .with(csrf())
                .with(anonymous())
                .cookie(requestCookie))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.usuario").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.token").exists())
            .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.SET_COOKIE))
            .andReturn();

        var responseCookie = result.getResponse().getCookie(requestCookie.getName());
        Assertions.assertNotNull(responseCookie);

        var claims = this.jwtUtil.getTokenValidatedClaims(responseCookie.getValue());
        Assertions.assertTrue(claims.isPresent());
        Assertions.assertTrue(this.jwtUtil.tokenValido(claims.get()));
        Assertions.assertNotEquals(requestCookie.getValue(), responseCookie.getValue());
    }


    @Test
    @DisplayName("Não deveria reautenticar um usuário com um refresh token inválido")
    void shouldNotReauthenticateAnInvalidRefreshToken() throws Exception {
        var usuario = this.getUser();
        usuario.setAtivado(true);
        usuario = this.usuarioRepository.save(usuario);

        List<Pair<String, String>> invalidRefreshTokens = new ArrayList<>();
        invalidRefreshTokens.add(Pair.of("Um access token não deve servir como refresh token",
            this.jwtUtil.generateToken(usuario.getEmail())));

        invalidRefreshTokens.add(Pair.of("Um refresh token expirado não deve reautenticar o usuário",
            this.jwtUtil.generateRefreshToken(usuario.getId(), usuario.getEmail(), -1000)));

        invalidRefreshTokens.add(Pair.of("Uma string que sequer é um token JWT não deveria reautenticar o usuário",
            "claramenteNaoEhUmTokenJWT"));

        for (var pair : invalidRefreshTokens) {
            final var message = pair.getFirst();
            final var token = pair.getSecond();

            final var requestCookie = this.authService.obterRefreshCookie(token);

            var result = this.mockMvc
                .perform(post("/auth/refresh")
                    .with(csrf())
                    .with(anonymous())
                    .cookie(requestCookie))
                .andExpect(mvcResult -> Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(),
                    mvcResult.getResponse().getStatus(), message))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                    Matchers.containsString("Token inválido.")))
                .andReturn();

            var responseCookie = result.getResponse().getCookie(requestCookie.getName());
            Assertions.assertNotNull(responseCookie, "Deveria ter um refresh cookie de remoção setado");
            Assertions.assertNull(responseCookie.getValue(), "Deveria remover o cookie com refresh token inválido");
        }
    }
}