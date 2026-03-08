package com.aqConnecta.controller;

import com.aqConnecta.E2ETest;
import com.aqConnecta.config.AWSClientConfig;
import com.aqConnecta.factories.models.*;
import com.aqConnecta.model.Candidatura;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.presenters.VagaPresenter;
import com.aqConnecta.repository.*;
import com.aqConnecta.security.JWTUtil;
import com.aqConnecta.service.DocumentoService;
import com.aqConnecta.service.EmailService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@AllArgsConstructor
public class UsuarioControllerTest extends E2ETest {
    final private MockMvc mockMvc;
    final private JWTUtil jwtUtil;
    final private ObjectMapper objectMapper;
    final private UsuarioRepository usuarioRepository;
    final private ExperienciaRepository experienciaRepository;
    final private CurriculoRepository curriculoRepository;
    final private UniversidadeRepository universidadeRepository;
    final private FormacaoAcademicaRepository formacaoAcademicaRepository;
    final private VagaRepository vagaRepository;

    @MockBean
    @SuppressWarnings("unused")
    private EmailService emailService;
    @MockBean
    @SuppressWarnings("unused")
    private DocumentoService documentoService;
    @MockBean
    @SuppressWarnings("unused")
    private AWSClientConfig awsClientConfig;

    @Builder
    private static class GerarDadosParams {
        @Builder.Default
        public Integer experiencias = 1;
        @Builder.Default
        public Integer curriculos = 1;
        @Builder.Default
        public Integer competencias = 1;
        @Builder.Default
        public Integer formacoesAcademicas = 1;
        @Builder.Default
        public Integer candidaturas = 1;
    }

    private record GerarDadosResult(Usuario usuario, Usuario admin) {
    }

    private GerarDadosResult gerarDados(GerarDadosParams params) {
        final var random = new java.util.Random();
        final var usuarioAlvo = UsuarioFactory.criarAtivado();
        final var usuarioAutorDeVagas = UsuarioFactory.criarAdministrador();

        usuarioAlvo.getExperiencias()
            .addAll(Stream.generate(() -> ExperienciaFactory.criar(usuarioAlvo))
                .limit(params.experiencias)
                .toList());

        usuarioAlvo.getCurriculo()
            .addAll(Stream.generate(() -> CurriculoFactory.criar(usuarioAlvo))
                .limit(params.curriculos)
                .toList());

        usuarioAlvo.getCompetencias()
            .addAll(Stream.generate(CompetenciaFactory::criar).limit(params.competencias).toList());

        usuarioAlvo.getFormacoesAcademicas()
            .addAll(Stream.generate(() -> FormacaoAcademicaFactory.criar(usuarioAlvo))
                .limit(params.formacoesAcademicas)
                .toList());

        final var curriculosDisponiveis = usuarioAlvo.getCurriculo().stream().toList();
        final var candidaturas = Stream.generate(() -> {
                final var curriculo = curriculosDisponiveis.isEmpty()
                    ? CurriculoFactory.criar(usuarioAlvo)
                    : curriculosDisponiveis.get(random.nextInt(curriculosDisponiveis.size()));

                return CandidaturaFactory.criar(usuarioAlvo, VagaFactory.criar(usuarioAutorDeVagas), curriculo);
            })
            .limit(params.candidaturas)
            .toList();
        usuarioAlvo.getCandidaturas().addAll(candidaturas);

        this.usuarioRepository.save(usuarioAutorDeVagas);
        this.vagaRepository.saveAll(candidaturas.stream().map(Candidatura::getVaga).toList());

        final var resultado = this.usuarioRepository.saveAll(List.of(new Usuario[]{usuarioAlvo, usuarioAutorDeVagas}));

        return new GerarDadosResult(resultado.get(0), resultado.get(1));
    }

    @Test
    @DisplayName("[GET /usuario/p/{userUrl}] deveria exigir autenticação")
    void deveriaExigirAutenticacaoParaVerUsuarioPorUrl() throws Exception {
        final var usuario = UsuarioFactory.criarAtivado();
        this.usuarioRepository.save(usuario);

        this.mockMvc
            .perform(get("/usuario/p/" + usuario.getUserUrl())
                .with(csrf())
                .with(anonymous()))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("[GET /usuario/p/{userUrl}] deveria retornar um erro NOT FOUND para buscas por usuários inexistentes")
    void deveriaRetornarNotFoundParaBuscaDeUsuarioDesativadoPorUrl() throws Exception {
        final var usuario = UsuarioFactory.criarAtivado();
        final var token = this.jwtUtil.generateToken(usuario.getEmail());
        this.usuarioRepository.save(usuario);

        final var usuarioNaoPersistido = UsuarioFactory.criarAtivado();

        this.mockMvc
            .perform(get("/usuario/p/" + usuarioNaoPersistido.getUserUrl())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("[GET /usuario/p/{userUrl}] deveria responder com um usuário completo apresentado")
    void deveriaDevolverUmUsuarioCompletoPorUrl() throws Exception {
        final var dados = this.gerarDados(
            GerarDadosParams.builder().curriculos(2).competencias(3).formacoesAcademicas(2).build());

        final var usuario = UsuarioFactory.criarAtivado();
        final var token = this.jwtUtil.generateToken(usuario.getEmail());
        this.usuarioRepository.save(usuario);

        this.mockMvc
            .perform(get("/usuario/p/" + dados.usuario().getUserUrl())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("[GET /usuario/candidaturas] deveria listar todas as candidaturas do usuário logado")
    void deveriaListarTodasAsCandidaturasDoUsuarioLogado() throws Exception {
        final var CANDIDATURAS = 10;
        final var dados = this.gerarDados(GerarDadosParams.builder().candidaturas(CANDIDATURAS).build());
        final var token = this.jwtUtil.generateToken(dados.usuario.getEmail());

        // Insere dados de outros usuários. O teste garante que só as candidaturas do usuário
        // autenticado são retornados. Nada de qualquer outro usuário além dele deve ser retornado.
        this.gerarDados(GerarDadosParams.builder().candidaturas(50).build());

        final var body = this.mockMvc
            .perform(get("/usuario/candidaturas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", hasSize(CANDIDATURAS)))
            .andExpect(jsonPath("$.data[*].publicador.id", everyItem(is(dados.admin().getId().toString()))))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Assertions.assertDoesNotThrow(() ->
            objectMapper.readValue(JsonPath.read(body, "$.data").toString(), new TypeReference<List<VagaPresenter>>() {
            })
        );
    }

    @Test
    @DisplayName("[GET /usuario/candidaturas] deveria exigir que o usuário esteja logado")
    void deveriaExigirLoginParaVisualizarAsPropriasCandidaturas() throws Exception {
        this.mockMvc
            .perform(get("/usuario/candidaturas").with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }


    @Test
    @DisplayName("[GET /usuario/candidaturas] deveria retornar um array vazio se o usuário não tiver candidaturas")
    void deveriaRetornarUmArrayVazioSeNaoHouverCandidaturas() throws Exception {
        final var dados = this.gerarDados(GerarDadosParams.builder().candidaturas(0).build());
        final var token = this.jwtUtil.generateToken(dados.usuario().getEmail());

        this.mockMvc
            .perform(get("/usuario/candidaturas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @Disabled
    @DisplayName("[GET /usuario/candidaturas] deveria retornar os dados paginados")
    void deveriaRetornarAsCandidaturasPaginadas() throws Exception {
    }

    @Test
    @Disabled
    @DisplayName("[GET /usuario/candidaturas] deveria respeitar paginação")
    void deveriaRespeitarPaginacaoNaListagemDeCandidaturas() throws Exception {
    }

    @Test
    @Disabled
    @DisplayName("[GET /usuario/candidaturas] deveria respeitar limite de candidaturas por página")
    void deveriaRespeitarLimiteDeElementosNaListagemDeCandidaturas() throws Exception {
    }

}
