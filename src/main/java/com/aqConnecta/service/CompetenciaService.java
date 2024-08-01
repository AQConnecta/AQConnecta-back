package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.CompetenciaUsuarioRequest;
import com.aqConnecta.DTOs.request.CompetenciaVagaRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.UUID;

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

    public ResponseEntity<Object> listarCompetencias() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // TODO remover essa bosta de contains dps do riume arrumar o security
            if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
            }
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, competenciaRepository.findAll());
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as competencias do usuário.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private boolean isUserAnonymous(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName());
    }


}