package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.ExperienciaRequest;
import com.aqConnecta.DTOs.request.UsuarioRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.presenters.ExperienciaPresenter;
import com.aqConnecta.security.RequireAuth;
import com.aqConnecta.service.ExperienciaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/experiencia")
@Slf4j
public class ExperienciaController {

    private final ExperienciaService service;

    @Autowired
    public ExperienciaController(ExperienciaService service) {
        this.service = service;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarExperiencia(@RequestBody ExperienciaRequest experienciaRequest) {
        return service.cadastrarExperiencia(experienciaRequest);
    }

    // usar o endpoint /usuario/editar
    @Deprecated(forRemoval = true)
    @PostMapping("/cadastrar_desc_usuario")
    public ResponseEntity<Object> cadastrarDescricaoUsuario(@RequestBody UsuarioRequest usuarioRequest) {
        return service.cadastrarDescricaoUsuario(usuarioRequest);
    }

    @RequireAuth
    @GetMapping("/listar/{idUsuario}")
    public ResponseEntity<Object> listarExperienciasPorUsuario(@PathVariable UUID idUsuario) {
        final List<ExperienciaPresenter> experiencias = service
            .listarExperienciasPorUsuario(idUsuario)
            .stream()
            .map(ExperienciaPresenter::apresentar)
            .toList();

        return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, experiencias);
    }

    @GetMapping("/localizar/{idExperiencia}")
    public ResponseEntity<Object> localizarExperiencia(@PathVariable UUID idExperiencia) {
        return service.localizarExperiencia(idExperiencia);
    }

    @PutMapping("/alterar/{idExperiencia}")
    public ResponseEntity<Object> alterarExperiencia(@PathVariable UUID idExperiencia,
        @RequestBody ExperienciaRequest experienciaRequest) {
        return service.alterarExperiencia(idExperiencia, experienciaRequest);
    }

    @DeleteMapping("/deletar/{idExperiencia}")
    public ResponseEntity<Object> deletarExperiencia(@PathVariable UUID idExperiencia) {
        return service.deletarExperiencia(idExperiencia);
    }
}
