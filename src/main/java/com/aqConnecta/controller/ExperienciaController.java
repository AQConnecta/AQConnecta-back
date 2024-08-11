package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.ExperienciaRequest;
import com.aqConnecta.DTOs.request.UsuarioRequest;
import com.aqConnecta.service.ExperienciaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Object> cadastrarExperiencia(@RequestBody ExperienciaRequest experienciaRequest) {
        return service.cadastrarExperiencia(experienciaRequest);
    }

    @PostMapping("/cadastrar_desc_usuario")
    public ResponseEntity<Object> cadastrarDescricaoUsuario(@RequestBody UsuarioRequest usuarioRequest) {
        return service.cadastrarDescricaoUsuario(usuarioRequest);
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
    public ResponseEntity<Object> alterarExperiencia(@PathVariable UUID idExperiencia, @RequestBody ExperienciaRequest experienciaRequest) {
        return service.alterarExperiencia(idExperiencia, experienciaRequest);
    }

    @DeleteMapping("/deletar/{idExperiencia}")
    public ResponseEntity<Object> deletarExperiencia(@PathVariable UUID idExperiencia) {
        return service.deletarExperiencia(idExperiencia);
    }
}
