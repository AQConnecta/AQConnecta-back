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
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    @DisplayName("[PATCH /usuario/editar] deveria atualizar os dados do usuário com sucesso (204 No Content)")
    void deveriaAtualizarDadosDoUsuarioComSucesso() throws Exception {
        final var dados = this.gerarDados(GerarDadosParams.builder().build());
        final var token = this.jwtUtil.generateToken(dados.usuario().getEmail());

        final String payload = """
            {
                "nome": "Novo Nome Atualizado",
                "descricao": "Desenvolvedor de Software Backend",
                "telefone": "11999999999",
                "curriculoLattes": "http://lattes.cnpq.br/123456789",
                "githubProfile": "https://github.com/novo-usuario",
                "linkedinProfile": "https://linkedin.com/in/novo-usuario"
            }
            """;

        this.mockMvc
            .perform(patch("/usuario/editar")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isNoContent());

        final var usuarioAtualizado = this.usuarioRepository.findById(dados.usuario().getId()).orElseThrow();
        Assertions.assertEquals("Novo Nome Atualizado", usuarioAtualizado.getNome());
    }

    @Test
    @DisplayName("[PATCH /usuario/editar] deveria exigir que o usuário esteja logado (401 Unauthorized)")
    void deveriaExigirAutenticacaoParaEditarUsuario() throws Exception {
        final String payload = """
            {
                "nome": "Tentativa sem Autenticação"
            }
            """;

        this.mockMvc
            .perform(patch("/usuario/editar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(csrf())
                .with(anonymous()))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("[PATCH /usuario/editar] deveria retornar Bad Request (400) caso o DTO falhe na validação")
    void deveriaRetornarBadRequestParaPayloadInvalido() throws Exception {
        final var dados = this.gerarDados(GerarDadosParams.builder().build());
        final var token = this.jwtUtil.generateToken(dados.usuario().getEmail());

        final String payloadInvalido = """
            {
                "nome": "A",
                "descricao": "",
                "githubProfile": "nao-eh-uma-url-valida"
            }
            """;

        this.mockMvc
            .perform(patch("/usuario/editar")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadInvalido)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").isString())
            .andExpect(jsonPath("$.data.nome[0]").value(Matchers.containsStringIgnoringCase("curto")))
            .andExpect(jsonPath("$.data.descricao[0]").value(Matchers.containsStringIgnoringCase("vazia")));
    }

    @Test
    @DisplayName("[PATCH /usuario/editar] deveria realizar atualização parcial, mantendo intactos os campos não enviados")
    void deveriaManterCamposIntactosSeNaoForemEnviados() throws Exception {
        final var dados = this.gerarDados(GerarDadosParams.builder().build());
        final var usuario = dados.usuario();
        final var token = this.jwtUtil.generateToken(usuario.getEmail());

        final String descricaoOriginal = "Descrição intocável";
        final String telefoneOriginal = "11888888888";
        final java.net.URI githubOriginal = java.net.URI.create("https://github.com/intocavel");

        usuario.setDescricao(descricaoOriginal);
        usuario.setTelefone(telefoneOriginal);
        usuario.setPerfilGitHub(githubOriginal);
        this.usuarioRepository.save(usuario);

        final String payloadParcial = """
            {
                "nome": "Apenas o Nome Mudou"
            }
            """;

        this.mockMvc
            .perform(patch("/usuario/editar")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadParcial)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isNoContent());

        final var usuarioAtualizado = this.usuarioRepository.findById(usuario.getId()).orElseThrow();

        Assertions.assertEquals("Apenas o Nome Mudou",
            usuarioAtualizado.getNome(),
            "O nome deveria ter sido atualizado");

        Assertions.assertEquals(descricaoOriginal,
            usuarioAtualizado.getDescricao(),
            "A descrição não deveria ter mudado");
        Assertions.assertEquals(telefoneOriginal, usuarioAtualizado.getTelefone(), "O telefone não deveria ter mudado");
        Assertions.assertEquals(githubOriginal, usuarioAtualizado.getPerfilGitHub(), "O GitHub não deveria ter mudado");
    }

    @Test
    @DisplayName("[PATCH /usuario/editar] deveria apagar os dados se o valor for enviado explicitamente como null")
    void deveriaApagarCamposEnviadosComoNull() throws Exception {
        final var dados = this.gerarDados(GerarDadosParams.builder().build());
        final var usuario = dados.usuario();
        final var token = this.jwtUtil.generateToken(usuario.getEmail());

        usuario.setDescricao("Descrição que será apagada");
        this.usuarioRepository.save(usuario);

        final String payloadComNull = """
            {
                "nome": "Nome Mantido",
                "descricao": null
            }
            """;

        this.mockMvc
            .perform(patch("/usuario/editar")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadComNull)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isNoContent());

        final var usuarioAtualizado = this.usuarioRepository.findById(usuario.getId()).orElseThrow();
        Assertions.assertNull(usuarioAtualizado.getDescricao(),
            "A descrição deveria ter sido definida como nula no banco");
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
