package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.ExperienciaRequest;
import com.aqConnecta.DTOs.request.UsuarioRequest;
import com.aqConnecta.service.ExperienciaService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Object> cadastrarExperiencia(@Valid @RequestBody ExperienciaRequest experienciaRequest, Authentication authentication) {
        return service.cadastrarExperiencia(experienciaRequest, authentication.getName());
    }

    @PostMapping("/cadastrar_desc_usuario")
    public ResponseEntity<Object> cadastrarDescricaoUsuario(@Valid @RequestBody UsuarioRequest usuarioRequest, Authentication authentication) {
        return service.cadastrarDescricaoUsuario(usuarioRequest, authentication.getName());
    }

    @GetMapping("/listar/{idUsuario}")
    public ResponseEntity<Object> listarExperienciasPorUsuario(@PathVariable UUID idUsuario) {
        return service.listarExperienciasPorUsuario(idUsuario);
    }

    @GetMapping("/localizar/{idExperiencia}")
    public ResponseEntity<Object> localizarExperiencia(@PathVariable UUID idExperiencia) {
        return service.localizarExperiencia(idExperiencia);
    }

    @PutMapping("/alterar/{idExperiencia}")
    public ResponseEntity<Object> alterarExperiencia(@PathVariable UUID idExperiencia, @Valid @RequestBody ExperienciaRequest experienciaRequest, Authentication authentication) {
        return service.alterarExperiencia(idExperiencia, experienciaRequest, authentication.getName());
    }

    @DeleteMapping("/deletar/{idExperiencia}")
    public ResponseEntity<Object> deletarExperiencia(@PathVariable UUID idExperiencia, Authentication authentication) {
        return service.deletarExperiencia(idExperiencia, authentication.getName());
    }
}
