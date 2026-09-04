package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.CompetenciaRequest;
import com.aqConnecta.DTOs.request.CompetenciaUsuarioRequest;
import com.aqConnecta.DTOs.request.CompetenciaVagaRequest;
import com.aqConnecta.model.enums.AreaAtuacao;
import com.aqConnecta.service.CompetenciaService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<Object> listarCompetencias(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "100") int size) {
        return service.listarCompetencias(search, page, size);
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarCompetencia(@Valid @RequestBody CompetenciaRequest competenciaRequest) {
        return service.cadastrarCompetencia(competenciaRequest);
    }

    @PutMapping("/alterar/{idCompetencia}")
    public ResponseEntity<Object> alterarCompetencia(@PathVariable UUID idCompetencia, @Valid @RequestBody CompetenciaRequest competenciaRequest) {
        return service.alterarCompetencia(idCompetencia, competenciaRequest);
    }

    @DeleteMapping("/deletar/{idCompetencia}")
    public ResponseEntity<Object> deletarCompetencia(@PathVariable UUID idCompetencia) {
        return service.deletarCompetencia(idCompetencia);
    }

    @PostMapping("/relacionar_competencia_usuario")
    public ResponseEntity<Object> relacionarCompetenciasUsuario(@Valid @RequestBody CompetenciaUsuarioRequest competenciaUsuarioRequest, Authentication authentication) {
        return service.relacionarCompetenciasUsuario(competenciaUsuarioRequest, authentication.getName());
    }

    @GetMapping("/listar_por_usuario/{idUsuario}")
    public ResponseEntity<Object> listarCompetenciasPorUsuario(@PathVariable UUID idUsuario) {
        return service.listarCompetenciasPorUsuario(idUsuario);
    }

    @DeleteMapping("/remover_relacao_usuario")
    public ResponseEntity<Object> removerRelacaoCompetenciasUsuario(@Valid @RequestBody CompetenciaUsuarioRequest competenciaUsuarioRequest, Authentication authentication) {
        return service.removerRelacaoCompetenciasUsuario(competenciaUsuarioRequest, authentication.getName());
    }

    @PostMapping("/relacionar_competencia_vaga")
    public ResponseEntity<Object> relacionarCompetenciasVaga(@Valid @RequestBody CompetenciaVagaRequest competenciaVagaRequest, Authentication authentication) {
        return service.relacionarCompetenciasVaga(competenciaVagaRequest, authentication.getName());
    }

    @GetMapping("/listar_por_vaga/{idVaga}")
    public ResponseEntity<Object> listarCompetenciasPorVaga(@PathVariable UUID idVaga) {
        return service.listarCompetenciasPorVaga(idVaga);
    }

    @DeleteMapping("/remover_relacao_vaga")
    public ResponseEntity<Object> removerRelacaoCompetenciasVaga(@Valid @RequestBody CompetenciaVagaRequest competenciaVagaRequest, Authentication authentication) {
        return service.removerRelacaoCompetenciasVaga(competenciaVagaRequest, authentication.getName());
    }

    @GetMapping("/competencias_quentes")
    public ResponseEntity<Object> listarCompetenciasQuentes() {
        return service.listarCompetenciasQuentes();
    }

    @GetMapping("/por_area")
    public ResponseEntity<Object> sugestoesPorArea(@RequestParam("area") AreaAtuacao area) {
        return service.sugestoesPorArea(area);
    }

    @PostMapping("/sugerir")
    public ResponseEntity<Object> sugerir(@Valid @RequestBody CompetenciaRequest competenciaRequest, Authentication authentication) {
        return service.sugerir(competenciaRequest, authentication.getName());
    }

    @GetMapping("/pendentes")
    public ResponseEntity<Object> listarPendentes(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "50") int size) {
        return service.listarPendentes(page, size);
    }

    @PutMapping("/aprovar/{idCompetencia}")
    public ResponseEntity<Object> aprovar(@PathVariable UUID idCompetencia) {
        return service.aprovar(idCompetencia);
    }

    @DeleteMapping("/recusar/{idCompetencia}")
    public ResponseEntity<Object> recusar(@PathVariable UUID idCompetencia) {
        return service.recusar(idCompetencia);
    }
}
