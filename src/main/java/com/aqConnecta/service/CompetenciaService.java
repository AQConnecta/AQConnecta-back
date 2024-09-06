package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.CompetenciaRequest;
import com.aqConnecta.DTOs.request.CompetenciaUsuarioRequest;
import com.aqConnecta.DTOs.request.CompetenciaVagaRequest;
import com.aqConnecta.DTOs.request.VagaRequest;
import com.aqConnecta.DTOs.response.CompetenciaCountDTO;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.model.Competencia;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.Vaga;
import com.aqConnecta.repository.CompetenciaRepository;
import com.aqConnecta.repository.UsuarioRepository;
import com.aqConnecta.repository.VagaRepository;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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


    public ResponseEntity<Object> cadastrarCompetencia(CompetenciaRequest registro) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            if (usuario.verificarUsuarioNaoEAdministrador()) {
                return ResponseHandler.generateResponse("Você não tem permissão para cadastrar uma competencia.", HttpStatus.FORBIDDEN);
            }
            Competencia competencia = new Competencia().builder()
                    .id(UUID.randomUUID())
                    .descricao(registro.getDescricao())
                    .build();

            if (!competenciaRepository.findByDescricaoIgnoreCase(registro.getDescricao()).isEmpty()) {
                return ResponseHandler.generateResponse("Essa competencia ja existe!", HttpStatus.CONFLICT, competencia);
            }
            competenciaRepository.save(competencia);
            return ResponseHandler.generateResponse("Competencia cadastrada com súcesso!", HttpStatus.CREATED, competencia);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> alterarCompetencia(UUID idCompetencia, CompetenciaRequest registro) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            if (usuario.verificarUsuarioNaoEAdministrador()) {
                return ResponseHandler.generateResponse("Você não tem permissão para cadastrar uma competencia.", HttpStatus.FORBIDDEN);
            }
            Optional<Competencia> competencia = competenciaRepository.findById(idCompetencia);
            if (competencia.isPresent()) {
                Competencia competenciaAlterada = new Competencia().builder()
                        .id(registro.getId())
                        .descricao(registro.getDescricao())
                        .build();
                if (!competenciaRepository.findByDescricaoIgnoreCase(registro.getDescricao()).isEmpty()) {
                    return ResponseHandler.generateResponse("Essa competencia ja existe!", HttpStatus.CONFLICT, competencia);
                }
                competenciaRepository.save(competenciaAlterada);
            }
            return ResponseHandler.generateResponse("Competencia cadastrada com súcesso!", HttpStatus.CREATED, competencia);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> deletarCompetencia(UUID idCompetencia) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            if (usuario.verificarUsuarioNaoEAdministrador()) {
                return ResponseHandler.generateResponse("Você não tem permissão para cadastrar uma competencia.", HttpStatus.FORBIDDEN);
            }
            Optional<Competencia> competencia = competenciaRepository.findById(idCompetencia);
            if (competencia.isPresent()) {
                competenciaRepository.deleteFromVagaCompetencia(competencia.get().getId());
                competenciaRepository.deleteFromUsuarioCompetencia(competencia.get().getId());
                competenciaRepository.deleteCompetencia(competencia.get().getId());
            }
            return ResponseHandler.generateResponse("Competencia deletada com súcesso!", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> relacionarCompetenciasUsuario(CompetenciaUsuarioRequest registro) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        if (registro.validarDadosObrigatorios()) {
            return ResponseHandler.generateResponse("Error: Campos obrigatorios não informados", HttpStatus.BAD_REQUEST);
        }
        try {
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
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


    public ResponseEntity<Object> removerRelacaoCompetenciasUsuario(CompetenciaUsuarioRequest registro) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        if (registro.validarDadosObrigatorios()) {
            return ResponseHandler.generateResponse("Error: Campos obrigatorios não informados", HttpStatus.BAD_REQUEST);
        }
        try {
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            if (usuario.getCompetencias() == null) {
                return ResponseHandler.generateResponse("Usuário não possuí competencias.", HttpStatus.OK);
            }
            usuario.getCompetencias().removeIf(competencia ->
                    registro.getCompetencias().contains(competencia));
            usuarioRepository.save(usuario);
            return ResponseHandler.generateResponse("Competencias removidas com sucesso!", HttpStatus.CREATED, usuario.getCompetencias());
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<Object> listarCompetenciasPorUsuario(UUID idUsuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Usuario usuario = usuarioService.localizar(idUsuario);
            if (!Collections.isEmpty(usuario.getCompetencias())) {
                return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, usuario.getCompetencias());
            }
            return ResponseHandler.generateResponse("Nenhuma competencia encontrada para este usuário.", HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as competencias do usuário.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    public ResponseEntity<Object> relacionarCompetenciasVaga(CompetenciaVagaRequest registro) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        if (registro.validarDadosObrigatorios()) {
            return ResponseHandler.generateResponse("Error: Campos obrigatorios não informados", HttpStatus.BAD_REQUEST);
        }
        try {
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            Vaga vaga = vagaService.localizar(registro.getIdVaga());
            if (!usuario.getId().equals(vaga.getPublicador().getId())) {
                return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar uma vaga que não é sua", HttpStatus.BAD_REQUEST);
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


    public ResponseEntity<Object> removerRelacaoCompetenciasVaga(CompetenciaVagaRequest registro) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        if (registro.validarDadosObrigatorios()) {
            return ResponseHandler.generateResponse("Error: Campos obrigatorios não informados", HttpStatus.BAD_REQUEST);
        }
        try {
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            Vaga vaga = vagaService.localizar(registro.getIdVaga());

            if (!usuario.getId().equals(vaga.getPublicador().getId())) {
                return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar uma vaga que não é sua", HttpStatus.BAD_REQUEST);
            }

            if (vaga.getCompetencias() == null) {
                return ResponseHandler.generateResponse("Vaga não possuí competencias.", HttpStatus.OK);
            }

            vaga.getCompetencias().removeIf(competencia ->
                    registro.getCompetencias().contains(competencia));
            vagaRepository.save(vaga);
            return ResponseHandler.generateResponse("Competencias removidas com sucesso!", HttpStatus.CREATED, vaga.getCompetencias());
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<Object> listarCompetenciasPorVaga(UUID idVaga) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Vaga vaga = vagaService.localizar(idVaga);
            if (!Collections.isEmpty(vaga.getCompetencias())) {
                return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, vaga.getCompetencias());
            }
            return ResponseHandler.generateResponse("Nenhuma competencia encontrada para esta vaga.", HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as competencias da vaga.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listarCompetencias(String search, int page, int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // TODO remover essa bosta de contains dps do riume arrumar o security
            if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
            }
            Pageable pageable = PageRequest.of(page, size);
            Page<Competencia> competenciaPage;

            if (search.isEmpty()) {
                competenciaPage = competenciaRepository.findAll(pageable);
            } else {
                competenciaPage = competenciaRepository.findByDescricaoContainingIgnoreCase(search, pageable);
            }

            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, competenciaPage.getContent());
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as competencias do usuário.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private UUID byteArrayToUUID(byte[] byteArray) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        long mostSigBits = byteBuffer.getLong();
        long leastSigBits = byteBuffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    public ResponseEntity<Object> listarCompetenciasQuentes() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // TODO remover essa bosta de contains dps do riume arrumar o security
            if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
            }
            List<Object[]> results = competenciaRepository.countCompetenciasInVagas();
            List<CompetenciaCountDTO> competenciaCountDTOS =
                    results.stream()
                            .map(result -> new CompetenciaCountDTO(
                                    new Competencia(UUID.fromString(new String((byte[]) result[0])), result[1].toString()),
                                    ((Number) result[2]).longValue()
                            ))
                            .toList();

            assignLevels(competenciaCountDTOS);

            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, competenciaCountDTOS);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as competencias do usuário.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
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
        if (percentage < 25) {
            return 0L;
        } else if (percentage < 50) {
            return 1L;
        } else if (percentage < 75) {
            return 2L;
        } else if (percentage < 90) {
            return 3L;
        } else {
            return 4L;
        }
    }

    private boolean isUserAnonymous(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName());
    }


}
