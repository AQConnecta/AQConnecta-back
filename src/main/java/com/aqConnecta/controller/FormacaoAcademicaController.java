package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.FormacaoAcademicaRequest;
import com.aqConnecta.service.FormacaoAcademicaService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/formacao_academica")
@Slf4j
public class FormacaoAcademicaController {

    private final FormacaoAcademicaService service;

    @Autowired
    public FormacaoAcademicaController(FormacaoAcademicaService service) {
        this.service = service;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarFormacaoAcademica(@Valid @RequestBody FormacaoAcademicaRequest formacaoAcademicaRequest, Authentication authentication) {
        return service.cadastrarFormacaoAcademica(formacaoAcademicaRequest, authentication.getName());
    }

    @PostMapping("/upload-diploma")
    public ResponseEntity<Object> uploadDiploma(@RequestParam("file") MultipartFile file) {
        return service.uploadDiploma(file);
    }

    @GetMapping("/listar/{idUsuario}")
    public ResponseEntity<Object> listarFormacaoAcademicasPorUsuario(@PathVariable UUID idUsuario) {
        return service.listarFormacaoAcademicaPorUsuario(idUsuario);
    }

    @GetMapping("/localizar/{idFormacaoAcademica}")
    public ResponseEntity<Object> localizarFormacaoAcademica(@PathVariable UUID idFormacaoAcademica) {
        return service.localizarFormacaoAcademica(idFormacaoAcademica);
    }

    @PutMapping("/alterar/{idFormacaoAcademica}")
    public ResponseEntity<Object> alterarFormacaoAcademica(@PathVariable UUID idFormacaoAcademica, @Valid @RequestBody FormacaoAcademicaRequest formacaoAcademicaRequest, Authentication authentication) {
        return service.alterarFormacaoAcademica(idFormacaoAcademica, formacaoAcademicaRequest, authentication.getName());
    }

    @DeleteMapping("/deletar/{idFormacaoAcademica}")
    public ResponseEntity<Object> deletarFormacaoAcademica(@PathVariable UUID idFormacaoAcademica, Authentication authentication) {
        return service.deletarFormacaoAcademica(idFormacaoAcademica, authentication.getName());
    }
}
