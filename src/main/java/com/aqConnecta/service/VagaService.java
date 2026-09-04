package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.VagaRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.DTOs.response.VagaResponse;
import com.aqConnecta.model.Candidatura;
import com.aqConnecta.model.Curriculo;
import com.aqConnecta.model.Projeto;
import com.aqConnecta.model.Vaga;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.repository.CandidaturaRepository;
import com.aqConnecta.repository.CurriculoRepository;
import com.aqConnecta.repository.ProjetoRepository;
import com.aqConnecta.repository.VagaRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VagaService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private CandidaturaRepository candidaturaRepository;

    @Autowired
    private CurriculoRepository curriculoRepository;

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private BusinessMetrics businessMetrics;

    public ResponseEntity<Object> cadastrarVaga(VagaRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Vaga vaga = Vaga.builder()
                    // Não setar ID manual — quem faz isso é o @GeneratedValue(UUID).
                    // Setando, o Spring Data JPA cai no caminho de merge() em vez de
                    // persist(), e o frontend pode receber um ID que não foi efetivamente
                    // persistido (vide bug do "vaga não encontrada").
                    .publicador(usuario)
                    .titulo(registro.getTitulo())
                    .descricao(registro.getDescricao())
                    .localDaVaga(registro.getLocalDaVaga())
                    .aceitaRemoto(registro.isAceitaRemoto())
                    .dataLimiteCandidatura(registro.getDataLimiteCandidatura())
                    .criadoEm(LocalDateTime.now())
                    .isIniciante(registro.isIniciante())
                    .projeto(resolverProjeto(registro.getIdProjeto()))
                    .areaAtuacao(registro.getAreaAtuacao())
                    .build();
            // saveAndFlush garante INSERT imediato e retorna a entity gerenciada
            // (com o ID definitivo). Indispensável porque o front chama em seguida
            // o /competencia/relacionar_competencia_vaga com esse ID.
            vaga = vagaRepository.saveAndFlush(vaga);
            businessMetrics.vagaCriada();
            return ResponseHandler.generateResponse("Vaga cadastrada com sucesso!", HttpStatus.CREATED, vaga);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<VagaResponse> fillVagaResponse(List<Vaga> vagas) {
        List<VagaResponse> vagasResponse = new ArrayList<>();
        for (Vaga vaga : vagas) {
            VagaResponse vagaResponse = new VagaResponse();
            vagaResponse.inToOut(vaga);
            vagasResponse.add(vagaResponse);
        }
        return vagasResponse;
    }

    private Projeto resolverProjeto(UUID idProjeto) {
        return idProjeto == null ? null : projetoRepository.findById(idProjeto).orElse(null);
    }

    public ResponseEntity<Object> listarVagasPorProjeto(UUID idProjeto) {
        try {
            List<VagaResponse> vagasResponse = fillVagaResponse(vagaRepository.findByProjetoId(idProjeto));
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, vagasResponse);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao listar as vagas do projeto.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // @Where no modelo Vaga já filtra deletados automaticamente
    public ResponseEntity<Object> listarVagas(String titulo, UUID idCompetencia, Boolean iniciante, Pageable pageable) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Vaga> vagas;

            if (!Strings.isEmpty(titulo)) {
                Page<Vaga> page = vagaRepository.findByTituloContainingIgnoreCase(titulo, pageable);
                vagas = page.getContent();
            } else if (idCompetencia != null) {
                Page<Vaga> page = vagaRepository.findByCompetenciaId(idCompetencia, pageable);
                vagas = page.getContent();
            } else {
                Page<Vaga> page = vagaRepository.findAll(pageable);
                vagas = page.getContent();
            }

            // Filtrar por prazo de candidatura
            vagas = vagas.stream()
                    .filter(vaga -> vaga.getDataLimiteCandidatura() == null || vaga.getDataLimiteCandidatura().isAfter(now))
                    .collect(Collectors.toList());

            if (iniciante != null) {
                vagas = vagas.stream()
                        .filter(vaga -> vaga.isIniciante() == iniciante)
                        .collect(Collectors.toList());
            }

            List<VagaResponse> vagasResponse = fillVagaResponse(vagas);
            if (!vagasResponse.isEmpty()) {
                return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, vagasResponse);
            }
            return ResponseHandler.generateResponse("Nenhuma vaga encontrada.", HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as vagas.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listarVagasPorUsuario(UUID idUsuario) {
        try {
            Usuario usuario = usuarioService.localizar(idUsuario);
            Set<Vaga> vagas = vagaRepository.findByPublicador(usuario);

            List<VagaResponse> vagasResponse = fillVagaResponse(vagas.stream().toList());
            if (!vagasResponse.isEmpty()) {
                return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, vagasResponse);
            }
            return ResponseHandler.generateResponse("Nenhum vaga encontrada para este usuário.", HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as vagas do usuário.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> localizarVaga(UUID idVaga) {
        try {
            Optional<Vaga> vaga = vagaRepository.findById(idVaga);
            if (vaga.isPresent()) {
                return ResponseHandler.generateResponse("Localizado com sucesso", HttpStatus.OK, vaga);
            }
            return ResponseHandler.generateResponse("Nenhuma vaga encontrada para este ID.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar localizar a vaga.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public Vaga localizar(UUID uuid) throws Exception {
        Vaga vaga = vagaRepository.findById(uuid)
                .orElseThrow(() -> new Exception("Vaga não encontrada para o id: " + uuid.toString()));
        return vaga;
    }

    public ResponseEntity<Object> alterarVaga(UUID idVaga, VagaRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Vaga> vaga = vagaRepository.findById(idVaga);
            if (vaga.isPresent()) {
                if (!vaga.get().getPublicador().getId().equals(usuario.getId()) && usuario.verificarUsuarioNaoEAdministrador()) {
                    return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
                }
                Vaga vagaAlterada = Vaga.builder()
                        .id(idVaga)
                        .publicador(vaga.get().getPublicador())
                        .titulo(registro.getTitulo())
                        .descricao(registro.getDescricao())
                        .localDaVaga(registro.getLocalDaVaga())
                        .aceitaRemoto(registro.isAceitaRemoto())
                        .dataLimiteCandidatura(registro.getDataLimiteCandidatura())
                        .criadoEm(vaga.get().getCriadoEm())
                        .atualizadoEm(LocalDateTime.now())
                        .isIniciante(registro.isIniciante())
                        .competencias(vaga.get().getCompetencias())
                        .candidaturas(vaga.get().getCandidaturas())
                        .projeto(resolverProjeto(registro.getIdProjeto()))
                        .areaAtuacao(registro.getAreaAtuacao())
                        .build();
                vagaRepository.save(vagaAlterada);
                return ResponseHandler.generateResponse("Vaga atualizada com sucesso!", HttpStatus.OK, vagaAlterada);
            }
            return ResponseHandler.generateResponse("Erro ao encontrar a vaga!", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Soft delete padronizado
    public ResponseEntity<Object> deletarVaga(UUID idVaga, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Vaga> vaga = vagaRepository.findById(idVaga);

            if (vaga.isPresent()) {
                if (!vaga.get().getPublicador().getId().equals(usuario.getId()) && usuario.verificarUsuarioNaoEAdministrador()) {
                    return ResponseHandler.generateResponse("Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
                }
                Vaga vagaParaDeletar = vaga.get();
                vagaParaDeletar.setDeletadoEm(LocalDateTime.now());
                vagaRepository.save(vagaParaDeletar);
                return ResponseHandler.generateResponse("Deletado com sucesso", HttpStatus.OK);
            } else {
                return ResponseHandler.generateResponse("Não é possível excluir uma vaga não existente.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar excluir a vaga.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> candidatar(UUID vagaId, Integer curriculoId, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Vaga vaga = vagaRepository.findById(vagaId).orElseThrow(() -> new Exception("Vaga não existe"));
            Curriculo curriculo = curriculoRepository.getReferenceById(curriculoId);

            boolean jaCandidatado = vaga.getCandidaturas().stream()
                    .anyMatch(candidatura -> candidatura.getUsuario().getId().equals(usuario.getId()));

            if (jaCandidatado) {
                return ResponseHandler.generateResponse("Você já se candidatou a esta vaga.", HttpStatus.BAD_REQUEST);
            }

            Candidatura novaCandidatura = Candidatura.builder()
                    .usuario(usuario)
                    .vaga(vaga)
                    .curriculo(curriculoId)
                    .curriculoUrl(curriculo.getCurriculo())
                    .build();

            vaga.getCandidaturas().add(novaCandidatura);
            vaga = vagaRepository.save(vaga);
            return ResponseHandler.generateResponse("Candidatura enviada com sucesso", HttpStatus.OK, vaga);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao enviar a candidatura.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listarCandidaturas(UUID vagaId, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Vaga vaga = vagaRepository.findById(vagaId).orElseThrow(() -> new Exception("Vaga não existe"));

            if (!usuario.getId().equals(vaga.getPublicador().getId()) && usuario.verificarUsuarioNaoEAdministrador()) {
                return ResponseHandler.generateResponse("Usuario não tem permissão para realizar essa tarefa.", HttpStatus.FORBIDDEN);
            }

            return ResponseHandler.generateResponse("Usuários que se candidataram listados com sucesso",
                HttpStatus.OK, candidaturaRepository.findAllCandidaturaByVagaId(vagaId));
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao listar os usuários que se candidataram.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
