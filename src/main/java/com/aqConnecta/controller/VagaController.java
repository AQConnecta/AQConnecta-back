package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.VagaRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.exception.base.NaoAutorizadoException;
import com.aqConnecta.exception.base.RecursoNaoEncontradoException;
import com.aqConnecta.exception.usuarios.UsuarioNaoVerificadoException;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.security.AuthUser;
import com.aqConnecta.security.RequireAuth;
import com.aqConnecta.service.UsuarioService;
import com.aqConnecta.service.VagaService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequireAuth
@RestController
@RequestMapping("/vaga")
@Slf4j
public class VagaController {
    private final VagaService service;
    private final UsuarioService usuarioService;

    @Autowired
    public VagaController(VagaService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarVaga(
        @Valid @RequestBody VagaRequest vagaRequest,
        @AuthUser Usuario usuario) {
        return service.cadastrarVaga(vagaRequest, usuario);
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listarVagas(
        @RequestParam(value = "titulo", required = false, defaultValue = "") String titulo,
        @RequestParam(value = "idCompetencia", required = false) UUID idCompetencia,
        @RequestParam(value = "iniciante", required = false) Boolean iniciante
    ) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            var _usuario = this.usuarioService.obterDaAutenticacao(authentication);
        }
        catch (UsuarioNaoVerificadoException ignored) {
        }
        catch (Exception e) {
            throw new NaoAutorizadoException("Você não tem permissão para visualizar as vagas.");
        }

        return service.listarVagas(titulo, idCompetencia, iniciante);
    }

    @GetMapping("/listar/{idUsuario}")
    public ResponseEntity<Object> listarVagasPorUsuario(@PathVariable UUID idUsuario) {
        Usuario usuario;

        try {
            usuario = usuarioService.localizar(idUsuario);
        }
        catch (Exception e) {
            throw new RecursoNaoEncontradoException("Usuário inexistente.");
        }

        return service.listarVagasPorUsuario(usuario);
    }

    @GetMapping("/localizar/{idVaga}")
    public ResponseEntity<Object> localizarVaga(@PathVariable UUID idVaga) {
        return service.localizarVaga(idVaga);
    }

    @PutMapping("/alterar/{idVaga}")
    public ResponseEntity<Object> alterarVaga(@PathVariable UUID idVaga,
        @Valid @RequestBody VagaRequest vagaRequest,
        @AuthUser Usuario usuario) {
        return service.alterarVaga(idVaga, vagaRequest, usuario);
    }

    @DeleteMapping("/deletar/{idVaga}")
    public ResponseEntity<Object> deletarVaga(@PathVariable UUID idVaga, @AuthUser Usuario usuario) {
        return service.deletarVaga(idVaga, usuario);
    }

    @PostMapping("/candidatar/{idVaga}")
    public ResponseEntity<Object> candidatar(@PathVariable UUID idVaga,
        @RequestBody Integer curriculoId,
        @AuthUser Usuario usuario) {
        return service.candidatar(idVaga, curriculoId, usuario);
    }

    @GetMapping("/candidaturas/{idVaga}")
    public ResponseEntity<Object> ListarCandidaturas(@PathVariable UUID idVaga, @AuthUser Usuario usuario) {
        var candidaturas = service.listarCandidaturas(idVaga, usuario);

        return ResponseHandler.generateResponse("Usuários que se candidataram listados com sucesso.",
            HttpStatus.OK,
            candidaturas);
    }
}
