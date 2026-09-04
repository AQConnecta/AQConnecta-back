package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.VagaRequest;
import com.aqConnecta.service.VagaService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/vaga")
@Slf4j
public class VagaController {

    private final VagaService service;

    @Autowired
    public VagaController(VagaService service) {
        this.service = service;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarVaga(@Valid @RequestBody VagaRequest vagaRequest, Authentication authentication) {
        return service.cadastrarVaga(vagaRequest, authentication.getName());
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listarVagas(
            @RequestParam(value = "titulo", required = false, defaultValue = "") String titulo,
            @RequestParam(value = "idCompetencia", required = false) UUID idCompetencia,
            @RequestParam(value = "iniciante", required = false) Boolean iniciante,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return service.listarVagas(titulo, idCompetencia, iniciante, pageable);
    }

    @GetMapping("/listar/{idUsuario}")
    public ResponseEntity<Object> listarVagasPorUsuario(@PathVariable UUID idUsuario) {
        return service.listarVagasPorUsuario(idUsuario);
    }

    @GetMapping("/localizar/{idVaga}")
    public ResponseEntity<Object> localizarVaga(@PathVariable UUID idVaga) {
        return service.localizarVaga(idVaga);
    }

    @PutMapping("/alterar/{idVaga}")
    public ResponseEntity<Object> alterarVaga(@PathVariable UUID idVaga, @Valid @RequestBody VagaRequest vagaRequest, Authentication authentication) {
        return service.alterarVaga(idVaga, vagaRequest, authentication.getName());
    }

    @DeleteMapping("/deletar/{idVaga}")
    public ResponseEntity<Object> deletarVaga(@PathVariable UUID idVaga, Authentication authentication) {
        return service.deletarVaga(idVaga, authentication.getName());
    }

    @PostMapping("/candidatar/{idVaga}")
    public ResponseEntity<Object> candidatar(@PathVariable UUID idVaga, @RequestBody Integer curriculoId, Authentication authentication) {
        return service.candidatar(idVaga, curriculoId, authentication.getName());
    }

    @GetMapping("/candidaturas/{idVaga}")
    public ResponseEntity<Object> ListarCandidaturas(@PathVariable UUID idVaga, Authentication authentication) {
        return service.listarCandidaturas(idVaga, authentication.getName());
    }

    @GetMapping("/por-projeto/{idProjeto}")
    public ResponseEntity<Object> listarPorProjeto(@PathVariable UUID idProjeto) {
        return service.listarVagasPorProjeto(idProjeto);
    }
}
