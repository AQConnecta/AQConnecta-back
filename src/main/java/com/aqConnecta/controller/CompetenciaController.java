package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.CompetenciaUsuarioRequest;
import com.aqConnecta.DTOs.request.CompetenciaVagaRequest;
import com.aqConnecta.service.CompetenciaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/competencia")
@Slf4j
public class CompetenciaController {

    private final CompetenciaService service;

    @Autowired
    public CompetenciaController(CompetenciaService service) {
        this.service = service;
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listarCompetencias() {
        return service.listarCompetencias();
    }

    @PostMapping("/relacionar_competencia_usuario")
    public ResponseEntity<Object> relacionarCompetenciasUsuario(@RequestBody CompetenciaUsuarioRequest competenciaUsuarioRequest) {
        return service.relacionarCompetenciasUsuario(competenciaUsuarioRequest);
    }

    @GetMapping("/listar_por_usuario/{idUsuario}")
    public ResponseEntity<Object> listarCompetenciasPorUsuario(@PathVariable UUID idUsuario) {
        return service.listarCompetenciasPorUsuario(idUsuario);
    }

    @DeleteMapping("/remover_relacao_usuario")
    public ResponseEntity<Object> removerRelacaoCompetenciasUsuario(@RequestBody CompetenciaUsuarioRequest competenciaUsuarioRequest) {
        return service.removerRelacaoCompetenciasUsuario(competenciaUsuarioRequest);
    }

    @PostMapping("/relacionar_competencia_vaga")
    public ResponseEntity<Object> relacionarCompetenciasVaga(@RequestBody CompetenciaVagaRequest competenciaVagaRequest) {
        return service.relacionarCompetenciasVaga(competenciaVagaRequest);
    }

    @GetMapping("/listar_por_vaga/{idVaga}")
    public ResponseEntity<Object> listarCompetenciasPorVaga(@PathVariable UUID idVaga) {
        return service.listarCompetenciasPorVaga(idVaga);
    }

    @DeleteMapping("/remover_relacao_vaga")
    public ResponseEntity<Object> removerRelacaoCompetenciasVaga(@RequestBody CompetenciaVagaRequest competenciaVagaRequest) {
        return service.removerRelacaoCompetenciasVaga(competenciaVagaRequest);
    }

    @GetMapping("/competencias_quentes")
    public ResponseEntity<Object> listarCompetenciasQuentes() {
        return service.listarCompetenciasQuentes();
    }

    // TODO fazer na proxima release
//    @GetMapping("/localizar/{idEndereco}")
//    public ResponseEntity<Object> relacionarCompetenciasPorExperiencia(@PathVariable UUID idEndereco) {
//        return service.localizarEndereco(idEndereco);
//    }
}
