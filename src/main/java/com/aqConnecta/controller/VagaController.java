package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.VagaRequest;
import com.aqConnecta.service.VagaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Object> cadastrarVaga(@RequestBody VagaRequest vagaRequest) {
        return service.cadastrarVaga(vagaRequest);
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listarVagas() {
        return service.listarVagas();
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
    public ResponseEntity<Object> alterarVaga(@PathVariable UUID idVaga, @RequestBody VagaRequest vagaRequest) {
        return service.alterarVaga(idVaga, vagaRequest);
    }

    @DeleteMapping("/deletar/{idVaga}")
    public ResponseEntity<Object> deletarVaga(@PathVariable UUID idVaga) {
        return service.deletarVaga(idVaga);
    }

    @PostMapping("/candidatar/{idVaga}")
    public ResponseEntity<Object> candidatar(@PathVariable UUID idVaga, @RequestBody Integer curriculoId) {
        return service.candidatar(idVaga, curriculoId);
    }

    @GetMapping("/candidaturas/{idVaga}")
    public ResponseEntity<Object> ListarCandidaturas(@PathVariable UUID idVaga) {
        return service.listarCandidaturas(idVaga);
    }
}
