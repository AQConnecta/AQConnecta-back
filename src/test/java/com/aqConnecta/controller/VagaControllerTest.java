package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.VagaRequest;
import com.aqConnecta.config.AWSClientConfig;
import com.aqConnecta.model.Permissao;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.Vaga;
import com.aqConnecta.repository.PermissaoRepository;
import com.aqConnecta.repository.UsuarioRepository;
import com.aqConnecta.repository.VagaRepository;
import com.aqConnecta.security.JWTUtil;
import com.aqConnecta.service.AuthService;
import com.aqConnecta.service.DocumentoService;
import com.aqConnecta.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class VagaControllerTest {

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
    private VagaRepository vagaRepository;

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
        return getUser(true, false);
    }

    private Usuario getUser(boolean ativado, boolean deletado) {
        final var permissoes = new HashSet<Permissao>();
        permissoes.add(this.permissaoRepository.findById(1L)
            .orElseThrow(() -> new RuntimeException(
                "Erro interno, não foi possivel criar conta com permissão de cliente")));

        var usuario = Usuario.builder()
            .id(UUID.randomUUID())
            .nome("John Doe")
            .email(this.email)
            .senha(this.passwordEncoder.encode(this.senha))
            .permissao(permissoes)
            .deletado(deletado)
            .ativado(ativado)
            .build();

        this.usuarioRepository.save(usuario);

        return usuario;
    }

    private String generateToken(Usuario usuario) {
        return this.jwtUtil.generateToken(usuario.getEmail());
    }

    private VagaRequest getValidVacancyDto() {
        return VagaRequest.builder()
            .titulo("Teste")
            .descricao("Foo")
            .localDaVaga("Maringá")
            .aceitaRemoto(true)
            .isIniciante(true)
            .dataLimiteCandidatura(LocalDateTime.now().plusWeeks(2))
            .build();
    }

    @Test
    @DisplayName("[POST /vaga/cadastrar] Deveria permitir cadastrar uma vaga")
    void createNewVacancy() throws Exception {
        final var user = this.getUser();
        final var token = this.generateToken(user);

        final var reqDto = getValidVacancyDto();

        final var reqJson = this.objectMapper.writeValueAsString(reqDto);

        var body = this.mockMvc
            .perform(post("/vaga/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.titulo").value(reqDto.getTitulo()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.descricao").value(reqDto.getDescricao()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.localDaVaga").value(reqDto.getLocalDaVaga()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.aceitaRemoto").value(reqDto.isAceitaRemoto()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID vacancyId = UUID.fromString(JsonPath.read(body, "$.data.id"));

        var vacancies = this.vagaRepository.findAll();

        Assertions.assertEquals(1, vacancies.size());
        Assertions.assertEquals(vacancyId, vacancies.getFirst().getId());
        Assertions.assertEquals(reqDto.getTitulo(), vacancies.getFirst().getTitulo());
        Assertions.assertEquals(user.getId(), vacancies.getFirst().getPublicador().getId());

        Assertions.assertNull(vacancies.getFirst().getDeletadoEm(), "Vagas recém-criadas não deveriam estar deletadas");
        Assertions.assertNull(vacancies.getFirst().getAtualizadoEm(),
            "Vagas recém-criadas não devem já terem data de última atualização");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidVagaProvider")
    @DisplayName("[POST /vaga/cadastrar] Não deveria permitir cadastrar uma vaga")
    void createNewVacancyWithInvalidData(String _case, VagaRequest invalidDto) throws Exception {
        final var user = this.getUser();
        final var token = this.generateToken(user);

        final var reqJson = this.objectMapper.writeValueAsString(invalidDto);

        this.mockMvc
            .perform(post("/vaga/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());

        var vacancies = this.vagaRepository.findAll();
        Assertions.assertEquals(0, vacancies.size());
    }

    static Stream<Arguments> invalidVagaProvider() {
        return Stream.of(
            Arguments.of("sem título", VagaRequest.builder()
                .descricao("Foo")
                .localDaVaga("Maringá")
                .aceitaRemoto(true)
                .isIniciante(true)
                .dataLimiteCandidatura(LocalDateTime.now().plusWeeks(2))
                .build()),
            Arguments.of("sem descrição", VagaRequest.builder()
                .titulo("Teste")
                .localDaVaga("Maringá")
                .aceitaRemoto(true)
                .isIniciante(true)
                .dataLimiteCandidatura(LocalDateTime.now().plusWeeks(2))
                .build()),
            Arguments.of("sem local da vaga", VagaRequest.builder()
                .titulo("Teste")
                .descricao("Foo")
                .aceitaRemoto(true)
                .isIniciante(true)
                .dataLimiteCandidatura(LocalDateTime.now().plusWeeks(2))
                .build()),
            Arguments.of("com data limite no passado", VagaRequest.builder()
                .titulo("Teste")
                .descricao("Foo")
                .localDaVaga("Maringá")
                .aceitaRemoto(true)
                .isIniciante(true)
                .dataLimiteCandidatura(LocalDateTime.now().minusMinutes(1)).build()),
            Arguments.of("com título vazio", VagaRequest.builder()
                .titulo("")
                .descricao("Foo")
                .localDaVaga("Maringá")
                .aceitaRemoto(true)
                .isIniciante(true)
                .dataLimiteCandidatura(LocalDateTime.now().plusWeeks(2))
                .build()),
            Arguments.of("com descrição vazia", VagaRequest.builder()
                .titulo("Teste")
                .descricao("")
                .localDaVaga("Maringá")
                .aceitaRemoto(true)
                .isIniciante(true)
                .dataLimiteCandidatura(LocalDateTime.now().plusWeeks(2))
                .build()),
            Arguments.of("com local da vaga vazio", VagaRequest.builder()
                .titulo("Teste")
                .descricao("Foo")
                .localDaVaga("")
                .aceitaRemoto(true)
                .isIniciante(true)
                .dataLimiteCandidatura(LocalDateTime.now().plusWeeks(2))
                .build())
        );
    }

    @Test
    @DisplayName("[POST /vaga/cadastrar] Não deveria permitir usuários sem e-mail confirmado criarem vagas")
    void createVacancyWithoutEmailConfirmation() throws Exception {
        final var user = this.getUser(false, false);
        final var token = this.generateToken(user);
        final var reqJson = this.objectMapper.writeValueAsString(getValidVacancyDto());

        this.mockMvc
            .perform(post("/vaga/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isForbidden());

        Assertions.assertEquals(0, this.vagaRepository.findAll().size());
    }

    @Test
    @DisplayName("[POST /vaga/cadastrar] Não deveria permitir usuários desabilitados criarem vagas")
    void createVacancyAsDisabledUser() throws Exception {
        final var user = this.getUser(true, true);
        final var token = this.generateToken(user);
        final var reqJson = this.objectMapper.writeValueAsString(getValidVacancyDto());

        this.mockMvc
            .perform(post("/vaga/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isForbidden());

        Assertions.assertEquals(0, this.vagaRepository.findAll().size());
    }

    @Test
    @DisplayName("[POST /vaga/cadastrar] Não deveria permitir usuários anônimos criarem vagas")
    void createVacancyAsAnonymousUser() throws Exception {
        final var reqJson = this.objectMapper.writeValueAsString(getValidVacancyDto());

        this.mockMvc
            .perform(post("/vaga/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson)
                .with(csrf())
                .with(anonymous()))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Assertions.assertEquals(0, this.vagaRepository.findAll().size());
    }

    @Test
    @DisplayName("[PUT /vaga/alterar/{id}] Deveria atualizar uma vaga")
    void updateVacancy() throws Exception {
        final var usuario = this.getUser();
        final var token = this.generateToken(usuario);

        var originalVacancy = Vaga.builder()
            .titulo("Foo")
            .descricao("Bar")
            .localDaVaga("Floptropica")
            .isIniciante(false)
            .aceitaRemoto(false)
            .dataLimiteCandidatura(LocalDateTime.now().plusWeeks(2))
            .publicador(usuario)
            .build();

        originalVacancy = this.vagaRepository.save(originalVacancy);

        final var reqDto = VagaRequest.builder()
            .titulo("bar")
            .descricao("baz")
            .localDaVaga("Bahia Cristal")
            .isIniciante(true)
            .aceitaRemoto(true)
            .dataLimiteCandidatura(LocalDateTime.now().plusWeeks(3).withNano(0))
            .build();

        final var reqJson = this.objectMapper.writeValueAsString(reqDto);

        this.mockMvc
            .perform(put("/vaga/alterar/" + originalVacancy.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf())
                .with(anonymous()))
            .andExpect(MockMvcResultMatchers.status().isOk());

        final var vacancies = this.vagaRepository.findAll();
        Assertions.assertEquals(1, vacancies.size());

        final var savedVacancy = vacancies.get(0);
        Assertions.assertEquals(originalVacancy.getId(), savedVacancy.getId());
        Assertions.assertEquals(reqDto.getTitulo(), savedVacancy.getTitulo());
        Assertions.assertEquals(reqDto.getDescricao(), savedVacancy.getDescricao());
        Assertions.assertEquals(reqDto.getLocalDaVaga(), savedVacancy.getLocalDaVaga());
        Assertions.assertEquals(reqDto.isIniciante(), savedVacancy.isIniciante());
        Assertions.assertEquals(reqDto.isAceitaRemoto(), savedVacancy.isAceitaRemoto());
        Assertions.assertEquals(reqDto.getDataLimiteCandidatura(), savedVacancy.getDataLimiteCandidatura());
    }
}
