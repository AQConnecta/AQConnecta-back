package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.VagaRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.exception.base.RecursoNaoEncontradoException;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.Vaga;
import com.aqConnecta.presenters.VagaPresenter;
import com.aqConnecta.security.AuthUser;
import com.aqConnecta.security.RequireAuth;
import com.aqConnecta.service.UsuarioService;
import com.aqConnecta.service.VagaService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        final var vaga = service.cadastrarVaga(vagaRequest, usuario);
        return ResponseHandler.generateResponse("Vaga cadastrada com sucesso!", HttpStatus.CREATED, vaga);
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listarVagas(
        @RequestParam(value = "titulo", required = false, defaultValue = "") String titulo,
        @RequestParam(value = "idCompetencia", required = false) UUID idCompetencia,
        @RequestParam(value = "iniciante", required = false) Boolean iniciante
    ) {
        final var vagas = service
            .listarVagas(titulo, idCompetencia, iniciante)
            .stream()
            .map(VagaPresenter::apresentar)
            .toList();

        return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, vagas);
    }

    @GetMapping("/listar/{idUsuario}")
    public ResponseEntity<Object> listarVagasPorUsuario(@PathVariable UUID idUsuario) {
        Usuario usuario;

        try {usuario = usuarioService.localizar(idUsuario);}
        catch (Exception e) {throw new RecursoNaoEncontradoException("Usuário inexistente.");}

        final var vagas = service.listarVagasPorUsuario(usuario)
            .stream()
            .map(VagaPresenter::apresentar)
            .toList();

        return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, vagas);
    }

    @GetMapping("/localizar/{idVaga}")
    public ResponseEntity<Object> localizarVaga(@PathVariable UUID idVaga) {
        final Vaga vaga = service.localizar(idVaga);
        return ResponseHandler.generateResponse("Localizado com sucesso", HttpStatus.OK, vaga);
    }

    @PutMapping("/alterar/{idVaga}")
    public ResponseEntity<Object> alterarVaga(@PathVariable UUID idVaga,
        @Valid @RequestBody VagaRequest vagaRequest,
        @AuthUser Usuario usuario) {
        final var vagaAtualizada = service.alterarVaga(idVaga, vagaRequest, usuario);
        return ResponseHandler.generateResponse("Vaga atualizada com súcesso!", HttpStatus.OK, vagaAtualizada);
    }

    @DeleteMapping("/deletar/{idVaga}")
    public ResponseEntity<Object> deletarVaga(@PathVariable UUID idVaga, @AuthUser Usuario usuario) {
        service.deletarVaga(idVaga, usuario);
        return ResponseHandler.generateResponse("Deletado com sucesso", HttpStatus.OK);
    }

    @PostMapping("/candidatar/{idVaga}")
    public ResponseEntity<Object> candidatar(@PathVariable UUID idVaga,
        @RequestBody Integer curriculoId,
        @AuthUser Usuario usuario) {
        final var vaga = service.candidatar(idVaga, curriculoId, usuario);
        return ResponseHandler.generateResponse("Candidatura enviada com sucesso", HttpStatus.OK, vaga);
    }

    @GetMapping("/candidaturas/{idVaga}")
    public ResponseEntity<Object> ListarCandidaturas(@PathVariable UUID idVaga, @AuthUser Usuario usuario) {
        var candidaturas = service.listarCandidaturas(idVaga, usuario);

        return ResponseHandler.generateResponse("Usuários que se candidataram listados com sucesso.",
            HttpStatus.OK,
            candidaturas);
    }
}
