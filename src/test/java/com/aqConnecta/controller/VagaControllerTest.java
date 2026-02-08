package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.VagaRequest;
import com.aqConnecta.E2ETest;
import com.aqConnecta.config.AWSClientConfig;
import com.aqConnecta.factories.models.UsuarioFactory;
import com.aqConnecta.factories.models.VagaFactory;
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
import net.datafaker.Faker;
import org.hamcrest.Matchers;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class VagaControllerTest extends E2ETest {

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

    private Usuario getUser() {
        return getUser(true, false);
    }

    private Usuario getUser(boolean ativado, boolean deletado) {
        final var permissoes = new HashSet<Permissao>();
        permissoes.add(this.permissaoRepository.findById(1L)
            .orElseThrow(() -> new RuntimeException(
                "Erro interno, não foi possivel criar conta com permissão de cliente")));

        var usuario = Usuario
            .builder()
            .nome("John Doe")
            .email("teste@mail.com")
            .senha(this.passwordEncoder.encode("12345678"))
            .permissao(permissoes)
            .deletado(deletado)
            .ativado(ativado)
            .build();

        usuario = this.usuarioRepository.save(usuario);

        return usuario;
    }

    private String generateToken(Usuario usuario) {
        return this.jwtUtil.generateToken(usuario.getEmail());
    }

    private static VagaRequest getValidVacancyDto() {
        return VagaRequest.builder()
            .titulo("Teste")
            .descricao("Foo")
            .localDaVaga("Maringá")
            .aceitaRemoto(true)
            .isIniciante(true)
            .dataLimiteCandidatura(LocalDateTime.now().plusWeeks(2).withNano(0))
            .build();
    }

    private static Stream<Arguments> invalidVagaProvider() {
        return Stream.of(
            Arguments.of("sem título", getValidVacancyDto().toBuilder().titulo(null).build()),
            Arguments.of("com título vazio", getValidVacancyDto().toBuilder().titulo("").build()),
            Arguments.of("sem descrição", getValidVacancyDto().toBuilder().descricao(null).build()),
            Arguments.of("com descrição vazia", getValidVacancyDto().toBuilder().descricao("").build()),
            Arguments.of("com descrição muito longa", getValidVacancyDto()
                .toBuilder()
                .descricao(
                    "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                    "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
                .build()),
            Arguments.of("sem local da vaga", getValidVacancyDto().toBuilder().localDaVaga(null).build()),
            Arguments.of("com local da vaga vazio", getValidVacancyDto().toBuilder().localDaVaga("").build()),
            Arguments.of("com data limite no passado",
                getValidVacancyDto().toBuilder().dataLimiteCandidatura(LocalDateTime.now().minusMinutes(1)).build())
        );
    }

    private static Stream<Arguments> validVagaProvider() {
        return Stream.of(
            Arguments.of("com todos os argumentos", getValidVacancyDto()),
            Arguments.of("sem data limite", getValidVacancyDto().toBuilder().dataLimiteCandidatura(null).build())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validVagaProvider")
    @DisplayName("[POST /vaga/cadastrar] Deveria permitir cadastrar uma vaga")
    void shouldCreateNewVacancy(String _case, VagaRequest reqDto) throws Exception {
        final var user = this.getUser();
        final var token = this.generateToken(user);

        final var reqJson = this.objectMapper.writeValueAsString(reqDto);

        var body = this.mockMvc
            .perform(post("/vaga/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.titulo").value(reqDto.getTitulo()))
            .andExpect(jsonPath("$.data.descricao").value(reqDto.getDescricao()))
            .andExpect(jsonPath("$.data.localDaVaga").value(reqDto.getLocalDaVaga()))
            .andExpect(jsonPath("$.data.aceitaRemoto").value(reqDto.isAceitaRemoto()))
            .andExpect(jsonPath("$.data.id").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID vacancyId = UUID.fromString(JsonPath.read(body, "$.data.id"));

        var vacancies = this.vagaRepository.findAll();

        assertEquals(1, vacancies.size());
        assertEquals(vacancyId, vacancies.getFirst().getId());
        assertEquals(reqDto.getTitulo(), vacancies.getFirst().getTitulo());
        assertEquals(user.getId(), vacancies.getFirst().getPublicador().getId());

        assertNull(vacancies.getFirst().getDeletadoEm(), "Vagas recém-criadas não deveriam estar deletadas");
        assertNull(vacancies.getFirst().getAtualizadoEm(),
            "Vagas recém-criadas não devem já terem data de última atualização");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidVagaProvider")
    @DisplayName("[POST /vaga/cadastrar] Não deveria permitir cadastrar uma vaga")
    void shouldNotCreateNewVacancyWithInvalidData(String _case, VagaRequest invalidDto) throws Exception {
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
        assertEquals(0, vacancies.size());
    }

    @Test
    @DisplayName("[POST /vaga/cadastrar] Não deveria permitir usuários sem e-mail confirmado criarem vagas")
    void shouldNotCreateVacancyWithoutEmailConfirmation() throws Exception {
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

        assertEquals(0, this.vagaRepository.findAll().size());
    }

    @Test
    @DisplayName("[POST /vaga/cadastrar] Não deveria permitir usuários desabilitados criarem vagas")
    void shouldNotCreateVacancyAsDisabledUser() throws Exception {
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

        assertEquals(0, this.vagaRepository.findAll().size());
    }

    @Test
    @DisplayName("[POST /vaga/cadastrar] Não deveria permitir usuários anônimos criarem vagas")
    void shouldNotCreateVacancyAsAnonymousUser() throws Exception {
        final var reqJson = this.objectMapper.writeValueAsString(getValidVacancyDto());

        this.mockMvc
            .perform(post("/vaga/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson)
                .with(csrf())
                .with(anonymous()))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        assertEquals(0, this.vagaRepository.findAll().size());
    }

    @Test
    @DisplayName("[PUT /vaga/alterar/{id}] Deveria atualizar uma vaga")
    void shouldUpdateVacancy() throws Exception {
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
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk());

        final var vacancies = this.vagaRepository.findAll();
        assertEquals(1, vacancies.size());

        final var savedVacancy = vacancies.getFirst();
        assertEquals(originalVacancy.getId(), savedVacancy.getId());
        assertEquals(reqDto.getTitulo(), savedVacancy.getTitulo());
        assertEquals(reqDto.getDescricao(), savedVacancy.getDescricao());
        assertEquals(reqDto.getLocalDaVaga(), savedVacancy.getLocalDaVaga());
        assertEquals(reqDto.isIniciante(), savedVacancy.isIniciante());
        assertEquals(reqDto.isAceitaRemoto(), savedVacancy.isAceitaRemoto());
        assertEquals(reqDto.getDataLimiteCandidatura(), savedVacancy.getDataLimiteCandidatura());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidVagaProvider")
    @DisplayName("[PUT /vaga/alterar/{id}] Não deveria permitir atualizar uma vaga")
    void shouldNotUpdateVacancyWithInvalidData(String _case, VagaRequest invalidDto) throws Exception {
        final var usuario = this.getUser();
        final var token = this.generateToken(usuario);

        var originalVacancy = Vaga.builder()
            .titulo("Bar")
            .descricao("Baz")
            .localDaVaga("Floptropica")
            .isIniciante(false)
            .aceitaRemoto(false)
            .dataLimiteCandidatura(LocalDateTime.now().plusWeeks(1).withNano(0))
            .publicador(usuario)
            .build();

        originalVacancy = this.vagaRepository.save(originalVacancy);

        final var reqJson = this.objectMapper.writeValueAsString(invalidDto);

        this.mockMvc
            .perform(put("/vaga/alterar/" + originalVacancy.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());

        final var savedVacancy = this.vagaRepository.findById(originalVacancy.getId()).orElseThrow();

        assertNotEquals(invalidDto.getTitulo(), savedVacancy.getTitulo());
        assertNotEquals(invalidDto.getDescricao(), savedVacancy.getDescricao());
        assertNotEquals(invalidDto.getLocalDaVaga(), savedVacancy.getLocalDaVaga());
        assertNotEquals(invalidDto.getDataLimiteCandidatura(), savedVacancy.getDataLimiteCandidatura());
    }

    private void insertVacancies(Usuario user, int disabledVacancies, int enabledVacancies, int expiredVacancies) {
        final var vacancies = new ArrayList<Vaga>();
        final var faker = new Faker();

        for (var i = 0; i < disabledVacancies; i++) {
            final var vacancy = VagaFactory.criar(user)
                .toBuilder()
                .criadoEm(LocalDateTime.ofInstant(faker.timeAndDate().past(10, 5, TimeUnit.DAYS),
                    ZoneId.systemDefault()))
                .deletadoEm(LocalDateTime.ofInstant(faker.timeAndDate().past(3, TimeUnit.DAYS), ZoneId.systemDefault()))
                .build();

            vacancies.add(vacancy);
        }

        for (var i = 0; i < enabledVacancies; i++) {
            final var vacancy = VagaFactory.criar(user)
                .toBuilder()
                .criadoEm(LocalDateTime.ofInstant(faker.timeAndDate().past(10, 0, TimeUnit.DAYS),
                    ZoneId.systemDefault()))
                .dataLimiteCandidatura(faker.bool().bool()
                    ? LocalDateTime.ofInstant(faker.timeAndDate().future(20, TimeUnit.DAYS), ZoneId.systemDefault())
                    : null)
                .build();

            vacancies.add(vacancy);
        }

        for (var i = 0; i < expiredVacancies; i++) {
            final var vacancy = VagaFactory.criar(user)
                .toBuilder()
                .criadoEm(LocalDateTime.ofInstant(faker.timeAndDate().past(10, 0, TimeUnit.DAYS),
                    ZoneId.systemDefault()))
                .dataLimiteCandidatura(LocalDateTime.ofInstant(faker.timeAndDate().past(3, TimeUnit.DAYS),
                    ZoneId.systemDefault()))
                .build();

            vacancies.add(vacancy);
        }

        vacancies.forEach(vaga -> this.vagaRepository.save(vaga));
    }

    @Test
    @DisplayName("[GET /vaga/listar] Deveria listar as vagas")
    void shouldListVacancies() throws Exception {
        var user = this.getUser();
        var token = this.generateToken(user);

        this.insertVacancies(user, 0, 5, 0);
        assertEquals(5, this.vagaRepository.findAll().size());

        this.mockMvc
            .perform(get("/vaga/listar")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", Matchers.hasSize(5)));
    }

    @Test
    @DisplayName("[GET /vaga/listar] Não deveria listar vagas apagadas")
    void shouldNotListSoftDeletedVacancies() throws Exception {
        var user = this.getUser();
        var token = this.generateToken(user);

        this.insertVacancies(user, 3, 2, 0);
        assertEquals(5, this.vagaRepository.findAll().size());

        var body = this.mockMvc
            .perform(get("/vaga/listar")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", Matchers.hasSize(2)))
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<Map<String, Object>> data = JsonPath.read(body, "$.data");

        var responseVacancies =
            data.stream()
                .map(record -> record.get("id").toString())
                .map(UUID::fromString)
                .map(id -> this.vagaRepository.findById(id).orElseThrow())
                .toList();

        assertThat(responseVacancies).allSatisfy(vacancy -> assertNull(vacancy.getDeletadoEm()));
    }

    @Test
    @DisplayName("[GET /vaga/listar] Não deveria listar vagas expiradas")
    void shouldNotListExpiredVacancies() throws Exception {
        var user = this.getUser();
        var token = this.generateToken(user);

        final var disabledVacancies = 5;
        final var enabledVacancies = 10;
        final var expiredVacancies = 5;
        this.insertVacancies(user, disabledVacancies, enabledVacancies, expiredVacancies);
        assertEquals(disabledVacancies + enabledVacancies + expiredVacancies, this.vagaRepository.findAll().size());

        var body = this.mockMvc
            .perform(get("/vaga/listar")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", Matchers.hasSize(enabledVacancies)))
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<Map<String, Object>> data = JsonPath.read(body, "$.data");

        var vacancies =
            data.stream()
                .map(record -> record.get("id").toString())
                .map(UUID::fromString)
                .map(id -> this.vagaRepository.findById(id).orElseThrow())
                .toList();

        final var now = LocalDateTime.now();
        assertThat(vacancies).allSatisfy(vacancy -> assertTrue(
            vacancy.getDataLimiteCandidatura() == null || vacancy.getDataLimiteCandidatura().isAfter(now)));
    }

    @Test
    @DisplayName("[GET /vaga/listar] Um usuário anônimo não deveria poder visualizar listagem de vagas")
    void shouldNotListVacanciesToAnonymousNorDisabledUsers() throws Exception {
        var user = this.getUser(true, true);
        var token = this.generateToken(user);

        this.mockMvc
            .perform(get("/vaga/listar")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(jsonPath("$.data").doesNotExist());

        this.mockMvc
            .perform(get("/vaga/listar")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .with(anonymous()))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("[GET /vaga/listar] Um usuário habilitado deveria poder visualizar listagem de vagas mesmo sem confirmar o e-mail")
    void shouldListVacanciesToEnabledButUnconfirmedUsers() throws Exception {
        var user = this.getUser(false, false);
        var token = this.generateToken(user);

        this.mockMvc
            .perform(get("/vaga/listar")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("[GET /vaga/localizar/{id}] Deveria encontrar uma vaga existente pelo seu ID")
    void shouldFindVacancyById() throws Exception {
        final var user = this.getUser(true, false);
        final var token = this.generateToken(user);

        var vacancy = VagaFactory.criar(user);

        vacancy = this.vagaRepository.save(vacancy);

        this.mockMvc
            .perform(get("/vaga/localizar/{id}", vacancy.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.id").value(vacancy.getId().toString()))
            .andExpect(jsonPath("$.data.titulo").value(vacancy.getTitulo()))
            .andExpect(jsonPath("$.data.descricao").value(vacancy.getDescricao()))
            .andExpect(jsonPath("$.data.localDaVaga").value(vacancy.getLocalDaVaga()))
            .andExpect(jsonPath("$.data.aceitaRemoto").value(vacancy.isAceitaRemoto()))
            .andExpect(jsonPath("$.data.iniciante").value(vacancy.isIniciante()))
            .andExpect(jsonPath("$.data.publicador.id").value(vacancy.getPublicador().getId().toString()))
            .andExpect(jsonPath("$.data.publicador.nome").value(vacancy.getPublicador().getNome()));
    }

    @Test
    @DisplayName("[GET /vaga/localizar/{id}] A busca por uma vaga inexistente deveria resultar em um erro NOT_FOUND")
    void shouldReturnNotFoundUponFindingUnexistingVacancy() throws Exception {
        final var user = this.getUser(false, false);
        final var token = this.generateToken(user);

        this.mockMvc
            .perform(get("/vaga/localizar/{id}", UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("[GET /vaga/localizar/{id}] Usuários anônimos e/ou deletados não deveriam poder visualizar uma vaga")
    void shouldNotDisplayVacancyToAnonymousOrDisabledUsers() throws Exception {
        var user = this.getUser(true, true);
        var token = this.generateToken(user);

        final var vacancy = this.vagaRepository.save(VagaFactory.criar(user));

        this.mockMvc
            .perform(get("/vaga/localizar/{id}", vacancy.getId().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(jsonPath("$.data").doesNotExist());

        this.mockMvc
            .perform(get("/vaga/localizar/{id}", vacancy.getId().toString())
                .with(csrf())
                .with(anonymous()))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(jsonPath("$.data").doesNotExist());
    }


    @Test
    @DisplayName("[GET /vaga/localizar/{id}] Usuários registrados deveriam poder visualizar uma vaga mesmo sem ter confirmado seu e-mail")
    void shouldDisplayVacancyToEnabledButUnconfirmedUser() throws Exception {
        var user = this.getUser(false, false);
        var token = this.generateToken(user);

        final var vacancy = this.vagaRepository.save(VagaFactory.criar(user));

        this.mockMvc
            .perform(get("/vaga/localizar/{id}", vacancy.getId().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("[GET /vaga/localizar/{id}] Um usuário não deveria poder visualizar uma vaga excluída")
    void shouldNotDisplayDisabledVacancyToUser() throws Exception {
        var user = this.getUser(false, false);
        var token = this.generateToken(user);

        var vacancy = VagaFactory.criar(user);
        vacancy.setDeletadoEm(LocalDateTime.now());
        vacancy = this.vagaRepository.save(vacancy);

        this.mockMvc
            .perform(get("/vaga/localizar/{id}", vacancy.getId().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("[GET /vaga/localizar/{id}] Um usuário deveria poder ver uma vaga expirada se esta foi buscada especificamente")
    void shouldDisplayExpiredVacancyToUser() throws Exception {
        var user = this.getUser(false, false);
        var token = this.generateToken(user);

        var vacancy = VagaFactory.criar(user);
        vacancy.setDataLimiteCandidatura(LocalDateTime.now().minusDays(2));
        vacancy = this.vagaRepository.save(vacancy);

        this.mockMvc
            .perform(get("/vaga/localizar/{id}", vacancy.getId().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data").exists());
    }

    private Stream<Arguments> usersThatCanDelete() {
        final var list = new ArrayList<Arguments>();

        final var authorUser = UsuarioFactory.criar();
        authorUser.setAtivado(true);

        {
            final var vacancy = VagaFactory.criar(authorUser);
            list.add(Arguments.of("for o autor", authorUser, vacancy));
        }
        {
            final var permissions = new HashSet<Permissao>();
            permissions.add(new Permissao(2, Permissao.ROLE_ADMIN));

            var adminUser = UsuarioFactory
                .criar()
                .toBuilder()
                .ativado(true)
                .permissao(permissions)
                .build();

            final var vacancy = VagaFactory.criar(authorUser);
            list.add(Arguments.of("for um administrador", adminUser, vacancy));
        }

        return list.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("usersThatCanDelete")
    @DisplayName("[DELETE /vaga/deletar/{id}] Deveria deletar a vaga se o usuário")
    void shouldAllowUserToDeleteVacancy(String _case, Usuario currentUser, Vaga vacancy) throws Exception {
        currentUser = this.usuarioRepository.save(currentUser);

        if (!vacancy.getPublicador().getId().equals(currentUser.getId())) {
            this.usuarioRepository.save(vacancy.getPublicador());
        }

        vacancy = this.vagaRepository.save(vacancy);

        final var token = this.generateToken(currentUser);

        this.mockMvc
            .perform(delete("/vaga/deletar/{id}", vacancy.getId().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk());

        assertEquals(0, this.vagaRepository.findAll().size());
    }

    @Test
    @DisplayName("[DELETE /vaga/deletar/{id}] Não deveria deixar um usuário sem e-mail confirmado deletar uma vaga")
    void shouldNotLetDeleteVacancyIfEmailIsntConfirmed() throws Exception {
        final var user = this.usuarioRepository.save(UsuarioFactory.criar().toBuilder().ativado(false).build());
        final var token = this.generateToken(user);
        final var vacancy = this.vagaRepository.save(VagaFactory.criar(user));

        this.mockMvc
            .perform(delete("/vaga/deletar/{id}", vacancy.getId().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isForbidden());

        assertEquals(1, this.vagaRepository.findAll().size());
    }

    @Test
    @DisplayName("[DELETE /vaga/deletar{id}] Não deveria deixar um usuário deletar uma vaga se ele não é o autor nem um administrador")
    void shouldNotDeleteVacancyIfUserIsNotAdminNorAuthor() throws Exception {
        final var user1 = this.getUser(false, false);
        final var user2 = this.usuarioRepository.save(UsuarioFactory.criar());

        final var vacancy = this.vagaRepository.save(VagaFactory.criar(user1));
        final var unauthorizedUserToken = this.generateToken(user2);

        this.mockMvc
            .perform(delete("/vaga/deletar/{id}", vacancy.getId().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + unauthorizedUserToken)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isForbidden());

        assertEquals(1, this.vagaRepository.findAll().size());
    }
}
