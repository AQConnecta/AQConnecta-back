package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.CompetenciaRequest;
import com.aqConnecta.DTOs.request.CompetenciaUsuarioRequest;
import com.aqConnecta.DTOs.request.CompetenciaVagaRequest;
import com.aqConnecta.DTOs.response.CompetenciaCountDTO;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.model.Competencia;
import com.aqConnecta.model.Permissao;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.Vaga;
import com.aqConnecta.model.enums.AreaAtuacao;
import com.aqConnecta.model.enums.StatusCompetencia;
import com.aqConnecta.repository.CompetenciaRepository;
import com.aqConnecta.repository.UsuarioRepository;
import com.aqConnecta.repository.VagaRepository;
import org.springframework.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.*;

@Slf4j
@Service
public class CompetenciaService {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private VagaService vagaService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CompetenciaRepository competenciaRepository;
    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private EmailService emailService;

    @Value("${cors.urls:http://localhost:3000}")
    private String corsUrls;

    // SecurityConfig já protege com hasAuthority("ADMIN")
    public ResponseEntity<Object> cadastrarCompetencia(CompetenciaRequest registro) {
        try {
            if (!competenciaRepository.findByDescricaoIgnoreCase(registro.getDescricao()).isEmpty()) {
                return ResponseHandler.generateResponse("Essa competencia já existe!", HttpStatus.CONFLICT, null);
            }
            Competencia competencia = Competencia.builder()
                    .descricao(registro.getDescricao())
                    .categoria(registro.getCategoria())
                    .status(StatusCompetencia.APROVADA)
                    .build();
            competencia = competenciaRepository.saveAndFlush(competencia);
            return ResponseHandler.generateResponse("Competencia cadastrada com sucesso!", HttpStatus.CREATED, competencia);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> alterarCompetencia(UUID idCompetencia, CompetenciaRequest registro) {
        try {
            Optional<Competencia> competencia = competenciaRepository.findById(idCompetencia);
            if (competencia.isPresent()) {
                Competencia competenciaAlterada = Competencia.builder()
                        .id(idCompetencia)
                        .descricao(registro.getDescricao())
                        .build();
                if (!competenciaRepository.findByDescricaoIgnoreCase(registro.getDescricao()).isEmpty()) {
                    return ResponseHandler.generateResponse("Essa competencia já existe!", HttpStatus.CONFLICT, competencia);
                }
                competenciaRepository.save(competenciaAlterada);
                return ResponseHandler.generateResponse("Competencia alterada com sucesso!", HttpStatus.OK, competenciaAlterada);
            }
            return ResponseHandler.generateResponse("Competencia não encontrada!", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> deletarCompetencia(UUID idCompetencia) {
        try {
            Optional<Competencia> competencia = competenciaRepository.findById(idCompetencia);
            if (competencia.isPresent()) {
                competenciaRepository.deleteFromVagaCompetencia(competencia.get().getId());
                competenciaRepository.deleteFromUsuarioCompetencia(competencia.get().getId());
                competenciaRepository.deleteCompetencia(competencia.get().getId());
            }
            return ResponseHandler.generateResponse("Competencia deletada com sucesso!", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> relacionarCompetenciasUsuario(CompetenciaUsuarioRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            if (usuario.getCompetencias() == null) {
                usuario.setCompetencias(new HashSet<>());
            }
            for (Competencia competencia : registro.getCompetencias()) {
                usuario.getCompetencias().add(competencia);
            }
            usuarioRepository.save(usuario);
            return ResponseHandler.generateResponse("Competencias relacionadas com sucesso!", HttpStatus.CREATED, usuario.getCompetencias());
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Object> removerRelacaoCompetenciasUsuario(CompetenciaUsuarioRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            if (usuario.getCompetencias() == null) {
                return ResponseHandler.generateResponse("Usuário não possui competências.", HttpStatus.OK);
            }
            usuario.getCompetencias().removeIf(competencia ->
                    registro.getCompetencias().contains(competencia));
            usuarioRepository.save(usuario);
            return ResponseHandler.generateResponse("Competencias removidas com sucesso!", HttpStatus.OK, usuario.getCompetencias());
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Object> listarCompetenciasPorUsuario(UUID idUsuario) {
        try {
            Usuario usuario = usuarioService.localizar(idUsuario);
            if (!CollectionUtils.isEmpty(usuario.getCompetencias())) {
                return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, usuario.getCompetencias());
            }
            return ResponseHandler.generateResponse("Nenhuma competencia encontrada para este usuário.", HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as competencias do usuário.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> relacionarCompetenciasVaga(CompetenciaVagaRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Vaga vaga = vagaService.localizar(registro.getIdVaga());
            if (!usuario.getId().equals(vaga.getPublicador().getId())) {
                return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar uma vaga que não é sua", HttpStatus.FORBIDDEN);
            }

            if (vaga.getCompetencias() == null) {
                vaga.setCompetencias(new HashSet<>());
            }

            for (Competencia competencia : registro.getCompetencias()) {
                vaga.getCompetencias().add(competencia);
            }
            vagaRepository.save(vaga);
            return ResponseHandler.generateResponse("Competencias relacionadas com sucesso!", HttpStatus.CREATED, vaga.getCompetencias());
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Object> removerRelacaoCompetenciasVaga(CompetenciaVagaRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Vaga vaga = vagaService.localizar(registro.getIdVaga());

            if (!usuario.getId().equals(vaga.getPublicador().getId())) {
                return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar uma vaga que não é sua", HttpStatus.FORBIDDEN);
            }

            if (vaga.getCompetencias() == null) {
                return ResponseHandler.generateResponse("Vaga não possui competências.", HttpStatus.OK);
            }

            vaga.getCompetencias().removeIf(competencia ->
                    registro.getCompetencias().contains(competencia));
            vagaRepository.save(vaga);
            return ResponseHandler.generateResponse("Competencias removidas com sucesso!", HttpStatus.OK, vaga.getCompetencias());
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Object> listarCompetenciasPorVaga(UUID idVaga) {
        try {
            Vaga vaga = vagaService.localizar(idVaga);
            if (!CollectionUtils.isEmpty(vaga.getCompetencias())) {
                return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, vaga.getCompetencias());
            }
            return ResponseHandler.generateResponse("Nenhuma competencia encontrada para esta vaga.", HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as competencias da vaga.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listarCompetencias(String search, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Competencia> competenciaPage;

            if (search.isEmpty()) {
                competenciaPage = competenciaRepository.findByStatus(StatusCompetencia.APROVADA, pageable);
            } else {
                competenciaPage = competenciaRepository.findByDescricaoContainingIgnoreCaseAndStatus(search, StatusCompetencia.APROVADA, pageable);
            }

            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, competenciaPage.getContent());
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as competencias.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listarCompetenciasQuentes() {
        try {
            List<Object[]> results = competenciaRepository.countCompetenciasInVagas();
            List<CompetenciaCountDTO> competenciaCountDTOS =
                    results.stream()
                            .map(result -> new CompetenciaCountDTO(
                                    Competencia.builder().id(UUID.fromString(new String((byte[]) result[0]))).descricao(result[1].toString()).build(),
                                    ((Number) result[2]).longValue()
                            ))
                            .toList();

            assignLevels(competenciaCountDTOS);
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, competenciaCountDTOS);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as competencias.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public void assignLevels(List<CompetenciaCountDTO> competenciaCountDTOS) {
        long total = vagaRepository.count();
        competenciaCountDTOS.forEach(dto -> {
            double percentage = (dto.getCount() * 100.0) / total;
            dto.setLevel(calculateLevel(percentage));
        });
    }

    private Long calculateLevel(double percentage) {
        if (percentage < 25) return 0L;
        else if (percentage < 50) return 1L;
        else if (percentage < 75) return 2L;
        else if (percentage < 90) return 3L;
        else return 4L;
    }

    public ResponseEntity<Object> sugestoesPorArea(AreaAtuacao area) {
        try {
            List<Competencia> resultado = new ArrayList<>();
            Set<UUID> vistos = new HashSet<>();
            for (Object[] row : competenciaRepository.countCompetenciasByVagaArea(area.name())) {
                UUID id = UUID.fromString(new String((byte[]) row[0]));
                if (vistos.add(id)) {
                    resultado.add(Competencia.builder().id(id).descricao(row[1].toString()).build());
                }
            }
            for (Competencia c : competenciaRepository.findByCategoriaAndStatusOrderByDescricaoAsc(area, StatusCompetencia.APROVADA)) {
                if (vistos.add(c.getId())) {
                    resultado.add(c);
                }
            }
            return ResponseHandler.generateResponse("Sugestões da área", HttpStatus.OK,
                    resultado.stream().limit(50).toList());
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao buscar sugestões da área.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> sugerir(CompetenciaRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            if (registro.getDescricao() == null || registro.getDescricao().isBlank()) {
                return ResponseHandler.generateResponse("Descrição é obrigatória.", HttpStatus.BAD_REQUEST);
            }
            if (!competenciaRepository.findByDescricaoIgnoreCase(registro.getDescricao().trim()).isEmpty()) {
                return ResponseHandler.generateResponse("Essa competência já existe ou já foi sugerida.", HttpStatus.CONFLICT);
            }
            Competencia competencia = Competencia.builder()
                    .descricao(registro.getDescricao().trim())
                    .categoria(registro.getCategoria())
                    .status(StatusCompetencia.PENDENTE)
                    .build();
            competencia = competenciaRepository.saveAndFlush(competencia);
            notificarAdmins(usuario, competencia);
            return ResponseHandler.generateResponse("Competência enviada para aprovação. Obrigado!", HttpStatus.CREATED, competencia);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao enviar a sugestão.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listarPendentes(int page, int size) {
        try {
            Page<Competencia> pendentes = competenciaRepository.findByStatus(StatusCompetencia.PENDENTE, PageRequest.of(page, size));
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, pendentes.getContent());
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao listar as competências pendentes.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> aprovar(UUID idCompetencia) {
        try {
            Optional<Competencia> competenciaOpt = competenciaRepository.findById(idCompetencia);
            if (competenciaOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Competência não encontrada.", HttpStatus.NOT_FOUND);
            }
            Competencia competencia = competenciaOpt.get();
            competencia.setStatus(StatusCompetencia.APROVADA);
            competenciaRepository.save(competencia);
            return ResponseHandler.generateResponse("Competência aprovada!", HttpStatus.OK, competencia);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> recusar(UUID idCompetencia) {
        try {
            if (competenciaRepository.findById(idCompetencia).isEmpty()) {
                return ResponseHandler.generateResponse("Competência não encontrada.", HttpStatus.NOT_FOUND);
            }
            competenciaRepository.deleteFromVagaCompetencia(idCompetencia);
            competenciaRepository.deleteFromUsuarioCompetencia(idCompetencia);
            competenciaRepository.deleteCompetencia(idCompetencia);
            return ResponseHandler.generateResponse("Sugestão recusada e removida.", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void notificarAdmins(Usuario autor, Competencia competencia) {
        try {
            List<Usuario> admins = usuarioRepository.findByPermissaoDescricao(Permissao.ROLE_ADMIN);
            String base = corsUrls == null || corsUrls.isBlank() ? "" : corsUrls.split(",")[0].trim();
            String link = base + "/admin/competencias";
            String areaTxt = competencia.getCategoria() != null ? competencia.getCategoria().name() : "—";
            String mensagem = (autor != null ? autor.getNome() : "Um usuário") + " sugeriu a competência \""
                    + competencia.getDescricao() + "\" (área: " + areaTxt + "). Revise para aprovar ou recusar.";
            for (Usuario admin : admins) {
                emailService.sendEmail(admin.getEmail(), "Nova competência para aprovação - AQConnecta",
                        emailService.criarCorpoEmail(admin.getNome(), mensagem, link));
            }
        } catch (Exception e) {
            log.warn("Falha ao notificar admins de sugestão de competência: {}", e.getMessage());
        }
    }
}
