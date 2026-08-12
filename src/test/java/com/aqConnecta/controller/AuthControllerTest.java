package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.LoginRequest;
import com.aqConnecta.E2ETest;
import com.aqConnecta.config.AWSClientConfig;
import com.aqConnecta.factories.models.UsuarioFactory;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.repository.PermissaoRepository;
import com.aqConnecta.repository.UsuarioRepository;
import com.aqConnecta.security.JWTUtil;
import com.aqConnecta.service.AuthService;
import com.aqConnecta.service.DocumentoService;
import com.aqConnecta.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AllArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AuthControllerTest extends E2ETest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private PasswordEncoder passwordEncoder;
    private PermissaoRepository permissaoRepository;
    private UsuarioRepository usuarioRepository;
    private JWTUtil jwtUtil;
    private AuthService authService;

    // Mockamos esses para não interagirem com nada externo
    @MockBean
    @SuppressWarnings("unused")
    private EmailService emailService;
    @MockBean
    @SuppressWarnings("unused")
    private DocumentoService documentoService;
    @MockBean
    @SuppressWarnings("unused")
    private AWSClientConfig awsClientConfig;

    final private String email = "teste@mail.com";
    final private String senha = "12345678";

    private Usuario getUser() {
        return UsuarioFactory.criar()
            .toBuilder()
            .email(this.email)
            .senha(this.passwordEncoder.encode(this.senha))
            .build();
    }

    @Test
    @DisplayName("Não deveria permitir um usuário logar sem que seu e-mail esteja confirmado")
    void loginWithUnconfirmedEmail() throws Exception {
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
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString(email)));
    }

    @Test
    @DisplayName("Não deveria permitir um usuário se autenticar com credenciais inválidas")
    void loginWithInvalidCredentials() throws Exception {
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

    @Test
    @DisplayName("Deveria remover o cookie de refresh diante de uma requisição de logout")
    void shouldRemoveRefreshTokenOnLogout() throws Exception {
        var usuario = this.getUser();
        usuario.setAtivado(true);
        this.usuarioRepository.save(usuario);

        var result = this.mockMvc
            .perform(post("/auth/logout")
                .with(csrf())
                .with(anonymous()))
            .andExpect(MockMvcResultMatchers.status().isNoContent())
            .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.SET_COOKIE))
            .andReturn();

        var responseCookie = result.getResponse().getCookie(this.authService.obterNomeDoCookie());
        Assertions.assertNotNull(responseCookie, "Deveria ter um refresh cookie de remoção setado");
        Assertions.assertNull(responseCookie.getValue(), "Deveria remover o cookie com refresh token inválido");
    }
}