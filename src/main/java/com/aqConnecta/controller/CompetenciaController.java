package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.CompetenciaUsuarioRequest;
import com.aqConnecta.DTOs.request.EnderecoRequest;
import com.aqConnecta.service.CompetenciaService;
import com.aqConnecta.service.EnderecoService;
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

    @PostMapping("/relacionar_competencia_usuario")
    public ResponseEntity<Object> relacionarCompetenciasUsuario(@RequestBody CompetenciaUsuarioRequest competenciaUsuarioRequest) {
        return service.relacionarCompetenciasUsuario(competenciaUsuarioRequest);
    }

    @GetMapping("/listar/{idUsuario}")
    public ResponseEntity<Object> listarCompetenciasPorUsuario(@PathVariable UUID idUsuario) {
        return service.listarCompetenciasPorUsuario(idUsuario);
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listarCompetencias() {
        return service.listarCompetencias();
    }

    @DeleteMapping("/remover_relacao_usuario")
    public ResponseEntity<Object> removerRelacaoCompetenciasUsuario(@RequestBody CompetenciaUsuarioRequest competenciaUsuarioRequest) {
        return service.removerRelacaoCompetenciasUsuario(competenciaUsuarioRequest);
    }


    // TODO fazer na proxima release
//    @GetMapping("/localizar/{idEndereco}")
//    public ResponseEntity<Object> relacionarCompetenciasPorExperiencia(@PathVariable UUID idEndereco) {
//        return service.localizarEndereco(idEndereco);
//    }
}
