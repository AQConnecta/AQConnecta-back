package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.VagaRequest;
import com.aqConnecta.E2ETest;
import com.aqConnecta.config.AWSClientConfig;
import com.aqConnecta.factories.models.*;
import com.aqConnecta.model.*;
import com.aqConnecta.presenters.VagaPresenter;
import com.aqConnecta.repository.*;
import com.aqConnecta.security.JWTUtil;
import com.aqConnecta.service.DocumentoService;
import com.aqConnecta.service.EmailService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.AllArgsConstructor;
import net.datafaker.Faker;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponentsBuilder;

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
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@AllArgsConstructor
public class VagaControllerTest extends E2ETest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private PasswordEncoder passwordEncoder;
    private PermissaoRepository permissaoRepository;
    private UsuarioRepository usuarioRepository;
    private CurriculoRepository curriculoRepository;
    private CandidaturaRepository candidaturaRepository;
    private CompetenciaRepository competenciaRepository;
    private VagaRepository vagaRepository;
    private JWTUtil jwtUtil;

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

    private Usuario getUser() {
        return getUser(true, false);
    }

    private Usuario getUser(boolean ativado, boolean deletado) {
        var usuario = UsuarioFactory.criar()
            .toBuilder()
            .ativado(ativado)
            .deletado(deletado)
            .senha(this.passwordEncoder.encode("12345678"))
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

    private final static String KNOWN_VACANCY_TITLE = "Foo";
    private final static UUID KNOWN_COMPETENCY_ID = UUID.randomUUID();

    private void insertVacancies(Usuario user, int disabledVacancies, int enabledVacancies, int expiredVacancies) {
        final var competencies = new HashSet<Competencia>();
        competencies.add(this.competenciaRepository.save(CompetenciaFactory.criar()));

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

        for (var i = 0; i < enabledVacancies - 1; i++) {
            final var vacancy = VagaFactory.criar(user)
                .toBuilder()
                .criadoEm(LocalDateTime.ofInstant(faker.timeAndDate().past(10, 0, TimeUnit.DAYS),
                    ZoneId.systemDefault()))
                .dataLimiteCandidatura(faker.bool().bool()
                    ? LocalDateTime.ofInstant(faker.timeAndDate().future(20, TimeUnit.DAYS), ZoneId.systemDefault())
                    : null)
                .competencias(faker.bool().bool() ? competencies : null)
                .build();

            vacancies.add(vacancy);
        }

        if (enabledVacancies > 0) {
            final var vacancy = VagaFactory.criar(user)
                .toBuilder()
                .titulo(KNOWN_VACANCY_TITLE)
                .criadoEm(LocalDateTime.ofInstant(faker.timeAndDate().past(10, 0, TimeUnit.DAYS),
                    ZoneId.systemDefault()))
                .dataLimiteCandidatura(faker.bool().bool()
                    ? LocalDateTime.ofInstant(faker.timeAndDate().future(20, TimeUnit.DAYS), ZoneId.systemDefault())
                    : null)
                .competencias(faker.bool().bool() ? competencies : null)
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

        this.vagaRepository.saveAll(vacancies);
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

    @AllArgsConstructor
    private static class VacancyFilter {
        public enum Type {
            Titulo,
            IdCompetencia,
            Iniciante
        }

        public Type type;
        public String stringifiedContent;
    }

    private Stream<Arguments> getVacanciesListFilters() {
        return Stream.of(
            Arguments.of("contendo texto no título", new VacancyFilter(VacancyFilter.Type.Titulo, KNOWN_VACANCY_TITLE)),
            Arguments.of("com competência",
                new VacancyFilter(VacancyFilter.Type.IdCompetencia, KNOWN_COMPETENCY_ID.toString())),
            Arguments.of("somente vagas que aceitam iniciantes",
                new VacancyFilter(VacancyFilter.Type.Iniciante, "true")),
            Arguments.of("somente vagas que não aceitam iniciantes",
                new VacancyFilter(VacancyFilter.Type.Iniciante, "false"))
        );
    }

    private Stream<Arguments> getVacanciesListComposedFilters() {
        return Stream.of(
            Arguments.of("título e iniciante", List.of(
                new VacancyFilter(VacancyFilter.Type.Titulo, KNOWN_VACANCY_TITLE),
                new VacancyFilter(VacancyFilter.Type.Iniciante, "true")
            )),
            Arguments.of("título e não iniciante", List.of(
                new VacancyFilter(VacancyFilter.Type.Titulo, KNOWN_VACANCY_TITLE),
                new VacancyFilter(VacancyFilter.Type.Iniciante, "false")
            )),
            Arguments.of("com competência e título", List.of(
                new VacancyFilter(VacancyFilter.Type.IdCompetencia, KNOWN_COMPETENCY_ID.toString()),
                new VacancyFilter(VacancyFilter.Type.Titulo, KNOWN_VACANCY_TITLE)
            )),
            Arguments.of("com competência e iniciante", List.of(
                new VacancyFilter(VacancyFilter.Type.IdCompetencia, KNOWN_COMPETENCY_ID.toString()),
                new VacancyFilter(VacancyFilter.Type.Iniciante, "true")
            ))
        );
    }

    private String resolveVacanciesListEndpointUrl(List<VacancyFilter> filters) {
        final var parameters = new HashMap<String, String>();

        filters.forEach(filter -> {
            switch (filter.type) {
                case Titulo:
                    parameters.put("titulo", filter.stringifiedContent);
                    break;
                case IdCompetencia:
                    parameters.put("idCompetencia", filter.stringifiedContent);
                    break;
                case Iniciante:
                    parameters.put("iniciante", filter.stringifiedContent);
                    break;
            }
        });

        var builder = UriComponentsBuilder.fromPath("/vaga/listar");
        parameters.forEach(builder::queryParam);
        return builder.toUriString();
    }

    private void validateVacanciesAgainstFilter(VacancyFilter filter, List<VagaPresenter> vacancies) {
        final var expectedValue = filter.stringifiedContent;

        switch (filter.type) {
            case Titulo:
                assertThat(vacancies).allSatisfy(vacancy -> assertThat(vacancy.titulo()).containsIgnoringCase(
                    expectedValue));
                break;
            case IdCompetencia:
                assertThat(vacancies).allSatisfy(vaga -> assertTrue(vaga.competencias()
                    .stream()
                    .anyMatch(competency -> competency.getId().toString().equals(expectedValue))));
                break;
            case Iniciante:
                assertThat(vacancies).allSatisfy(vaga -> assertEquals(vaga.isIniciante(),
                    filter.stringifiedContent.equals("true")));
                break;
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getVacanciesListFilters")
    @DisplayName("[GET /vaga/listar] Deveria lidar com filtros corretamente")
    void shouldHandleFiltersProperly(String _case, VacancyFilter filter) throws Exception {
        var user = this.getUser();
        var token = this.generateToken(user);

        final var totalVacancies = 15;
        this.insertVacancies(user, 0, totalVacancies, 0);
        assertEquals(totalVacancies, this.vagaRepository.findAll().size());

        var body = this.mockMvc
            .perform(get(this.resolveVacanciesListEndpointUrl(List.of(filter)))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data").isArray())
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<VagaPresenter> data = objectMapper.readValue(JsonPath.read(body, "$.data").toString(),
            new TypeReference<List<VagaPresenter>>() {
            });

        this.validateVacanciesAgainstFilter(filter, data);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getVacanciesListComposedFilters")
    @DisplayName("[GET /vaga/listar] Deveria lidar com vários filtros simultâneos apropriadamente")
    void shouldHandleMultipleFiltersProperly(String _case, List<VacancyFilter> filters) throws Exception {
        var user = this.getUser();
        var token = this.generateToken(user);

        final var totalVacancies = 15;
        this.insertVacancies(user, 0, totalVacancies, 0);
        assertEquals(totalVacancies, this.vagaRepository.findAll().size());

        var body = this.mockMvc
            .perform(get(this.resolveVacanciesListEndpointUrl(filters))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data").isArray())
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<VagaPresenter> data = objectMapper.readValue(JsonPath.read(body, "$.data").toString(),
            new TypeReference<List<VagaPresenter>>() {
            });

        assertThat(filters).allSatisfy(filter -> this.validateVacanciesAgainstFilter(filter, data));
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
                .criarAtivado()
                .toBuilder()
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
        final var user = this.usuarioRepository.save(UsuarioFactory.criar());
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

    @Test
    @DisplayName("[POST /vaga/candidatar/{idVaga}] Deveria deixar um usuário se candidatar a uma vaga existente")
    void shouldLetUsersApplyToVacancies() throws Exception {
        final var user = this.usuarioRepository.save(UsuarioFactory.criarAtivado());
        final var token = this.generateToken(user);

        final var vaga = this.vagaRepository.save(VagaFactory.criar(user));
        final var curriculum = this.curriculoRepository.save(CurriculoFactory.criar(user));

        this.mockMvc
            .perform(post("/vaga/candidatar/{idVaga}", vaga.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(curriculum.getId().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk());

        assertEquals(1, this.candidaturaRepository.findAllCandidaturaByVagaId(vaga.getId()).size());
    }

    @Test
    @DisplayName("[POST /vaga/candidatar/{idVaga}] Não deveria deixar um usuário se candidatar a uma vaga sem que ele tenha confirmado seu e-mail")
    void shouldNotLetUnconfirmedUserApplyToVacancies() throws Exception {
        final var user = this.usuarioRepository.save(UsuarioFactory.criar());
        final var token = this.generateToken(user);

        final var vaga = this.vagaRepository.save(VagaFactory.criar(user));
        final var curriculum = this.curriculoRepository.save(CurriculoFactory.criar(user));

        this.mockMvc
            .perform(post("/vaga/candidatar/{idVaga}", vaga.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(curriculum.getId().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isForbidden());

        assertEquals(0, this.candidaturaRepository.findAllCandidaturaByVagaId(vaga.getId()).size());
    }

    private Stream<Arguments> usersThatCanSeeVacancy() {
        return Stream.of(
            Arguments.of("for o autor", true),
            Arguments.of("for um administrador", false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("usersThatCanSeeVacancy")
    @DisplayName("[GET /vaga/candidaturas/{idVaga}] Deveria mostrar os candidatos de uma vaga se o usuário")
    void shouldLetUserSeeVacancyCandidatures(String _case, boolean isAuthor) throws Exception {
        final var author = this.usuarioRepository.save(UsuarioFactory.criarAtivado());
        final var admin = this.usuarioRepository.save(UsuarioFactory.criarAdministrador());

        final var vacancy = this.vagaRepository.save(VagaFactory.criar(author));

        final var totalCandidatures = 5;
        final var candidatures = new HashSet<Candidatura>();
        for (var i = 0; i < totalCandidatures; i++) {
            final var candidate = this.usuarioRepository.save(UsuarioFactory.criarAtivado());
            final var draftCandidature = CandidaturaFactory.criar(candidate, vacancy);
            this.curriculoRepository.save(CurriculoFactory
                .criar(candidate)
                .toBuilder()
                .curriculo(draftCandidature.getCurriculoUrl())
                .id(draftCandidature.getCurriculo())
                .build());
            candidatures.add(this.candidaturaRepository.save(draftCandidature));
        }
        vacancy.setCandidaturas(candidatures);
        this.vagaRepository.save(vacancy);

        final var loggedInUser = isAuthor ? author : admin;
        final var token = this.generateToken(loggedInUser);

        this.mockMvc
            .perform(get("/vaga/candidaturas/{idVaga}", vacancy.getId().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data", Matchers.hasSize(totalCandidatures)));
    }

    @Test()
    @DisplayName("[GET /vaga/candidaturas/{idVaga}] Não deveria mostrar os candidatos de uma vaga para nenhum outro usuário comum")
    void shouldNotShowCandidaturesToAnyOtherCommonUser() throws Exception {
        final var author = this.usuarioRepository.save(UsuarioFactory.criarAtivado());
        final var vacancy = this.vagaRepository.save(VagaFactory.criar(author));

        final var user = this.usuarioRepository.save(UsuarioFactory.criarAtivado());
        final var token = this.generateToken(user);

        this.mockMvc
            .perform(get("/vaga/candidaturas/{idVaga}", vacancy.getId().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(jsonPath("$.data").doesNotExist());
    }
}
