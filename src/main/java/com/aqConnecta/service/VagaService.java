package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.VagaRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.DTOs.response.VagaResponse;
import com.aqConnecta.model.Candidatura;
import com.aqConnecta.model.Vaga;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.repository.CandidaturaRepository;
import com.aqConnecta.repository.VagaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public ResponseEntity<Object> cadastrarVaga(VagaRequest registro) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
//        if (!registro.validarDadosObrigatorios()) {
//            return ResponseHandler.generateResponse("Error: Campos obrigatorios não informados", HttpStatus.BAD_REQUEST);
//        }
        try {
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            Vaga vaga = new Vaga().builder()
                    .id(UUID.randomUUID())
                    .publicador(usuario)
                    .titulo(registro.getTitulo())
                    .descricao(registro.getDescricao())
                    .localDaVaga(registro.getLocalDaVaga())
                    .aceitaRemoto(registro.isAceitaRemoto())
                    .dataLimiteCandidatura(registro.getDataLimiteCandidatura())
                    .criadoEm(registro.getCriadoEm())
                    .atualizadoEm(registro.getAtualizadoEm())
                    .isIniciante(registro.isIniciante())
                    .build();
            vagaRepository.save(vaga);
            return ResponseHandler.generateResponse("Vaga cadastrada com súcesso!", HttpStatus.CREATED, vaga);
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

    // Listar todas as vagas do sistema
    // TODO: Implementar os filtros
    public ResponseEntity<Object> listarVagas() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }

        try {
            LocalDateTime now = LocalDateTime.now();

            List<Vaga> vagas = vagaRepository.findAll().stream()
                    .filter(vaga -> vaga.getDeletadoEm() == null)
                    .filter(vaga -> vaga.getDataLimiteCandidatura() == null || vaga.getDataLimiteCandidatura().isAfter(now))
                    .collect(Collectors.toList());

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Optional<Vaga> vaga = vagaRepository.findById(idVaga);
            if (vaga.isPresent()) {
                return ResponseHandler.generateResponse("Localizado com sucesso", HttpStatus.OK, vaga);
            }
            return ResponseHandler.generateResponse("Nenhum experiencia encontrada para este ID.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar localizar a experiencia.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public Vaga localizar(UUID uuid) throws Exception {
        Vaga vaga = vagaRepository.findById(uuid)
                .orElseThrow(() -> new Exception("Vaga não encontrado para o id: " + uuid.toString()));

        if (vaga.getDeletadoEm() != null) {
            throw new Exception("Vaga não existe mais");
        }

        return vaga;
    }


    public ResponseEntity<Object> alterarVaga(UUID idVaga, VagaRequest registro) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // TODO remover essa bosta de contains dps do riume arrumar o security
            if (isUserAnonymous(authentication)) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
            }
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            Optional<Vaga> vaga = vagaRepository.findById(idVaga);
            if (vaga.isPresent()) {
                if (!vaga.get().getPublicador().getId().equals(usuario.getId())) {
                    return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.", HttpStatus.UNAUTHORIZED);
                }
                Vaga vagaAlterada = new Vaga().builder()
                        .id(idVaga)
                        .publicador(usuario)
                        .titulo(registro.getTitulo())
                        .descricao(registro.getDescricao())
                        .localDaVaga(registro.getLocalDaVaga())
                        .aceitaRemoto(registro.isAceitaRemoto())
                        .dataLimiteCandidatura(registro.getDataLimiteCandidatura())
                        .criadoEm(registro.getCriadoEm())
                        .atualizadoEm(registro.getAtualizadoEm())
                        .isIniciante(registro.isIniciante())
                        .build();
                vagaRepository.save(vagaAlterada);
                return ResponseHandler.generateResponse("Vaga atualizada com súcesso!", HttpStatus.CREATED, vaga);
            }
            return ResponseHandler.generateResponse("Erro ao encontrar a vaga!", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<Object> deletarVaga(UUID idVaga) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // TODO remover essa bosta de contains dps do riume arrumar o security
            if (isUserAnonymous(authentication)) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
            }

            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());

            Optional<Vaga> vaga = vagaRepository.findById(idVaga);

            if (vaga.isPresent()) {
                if (!vaga.get().getPublicador().getId().equals(usuario.getId()) && usuario.verificarUsuarioNaoEAdministrador()) {
                    return ResponseHandler.generateResponse("Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
                }
                vagaRepository.deleteById(idVaga);
                return ResponseHandler.generateResponse("Deletado com sucesso", HttpStatus.OK);
            } else {
                return ResponseHandler.generateResponse("Não é possível excluir uma vaga não existente.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar excluir a vaga.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private boolean isUserAnonymous(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName());
    }

    public ResponseEntity<Object> candidatar(UUID vagaId, Integer curriculoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se o usuário está autenticado e não é anônimo
        if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }

        try {
            assert authentication != null;
            String username = (String) authentication.getPrincipal();
            Usuario usuario = usuarioService.localizarPorEmail(username);
            Vaga vaga = vagaRepository.findById(vagaId).orElseThrow(() -> new Exception("Vaga não existe"));
            vaga.setCandidaturas(new HashSet<>());
            vaga.getCandidaturas().add(new Candidatura().builder()
                                                        .usuario(usuario)
                                                        .vaga(vaga)
                                                        .curriculo(curriculoId)
                                                        .curriculoUrl(usuario.getCurriculo().get(curriculoId))
                                                        .build());

            vaga = vagaRepository.save(vaga);

            return ResponseHandler.generateResponse("Candidatura enviada com sucesso", HttpStatus.OK, vaga);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao enviar a candidatura.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listarCandidaturas(UUID vagaId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se o usuário está autenticado e não é anônimo
        if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }

        try {
            assert authentication != null;
            String username = (String) authentication.getPrincipal();
            Usuario usuario = usuarioService.localizarPorEmail(username);
            Vaga vaga = vagaRepository.findById(vagaId).orElseThrow(() -> new Exception("Vaga não existe"));

            // Verifica se o usuário é o publicador da vaga ou tem permissão de administrador
            if (!usuario.getId().equals(vaga.getPublicador().getId()) && !usuario.verificarUsuarioNaoEAdministrador()) {
                return ResponseHandler.generateResponse("Usuario não tem permissão para realizar essa tarefa.", HttpStatus.UNAUTHORIZED);
            }

            // Obtém todas as candidaturas para a vaga e mapeia para os usuários
            List<Usuario> usuarios = candidaturaRepository.findAllCandidaturaByVagaId(vagaId)
                    .stream()
                    .map(Candidatura::getUsuario)
                    .distinct()  // Evita duplicados, caso haja candidaturas duplicadas
                    .collect(Collectors.toList());

            return ResponseHandler.generateResponse("Usuários que se candidataram listados com sucesso", HttpStatus.OK, candidaturaRepository.findAllCandidaturaByVagaId(vagaId));
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao listar os usuários que se candidataram.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


}
