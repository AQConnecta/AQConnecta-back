package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.FormacaoAcademicaRequest;
import com.aqConnecta.service.FormacaoAcademicaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Object> cadastrarFormacaoAcademica(@RequestBody FormacaoAcademicaRequest formacaoAcademicaRequest) {
        return service.cadastrarFormacaoAcademica(formacaoAcademicaRequest);
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
    public ResponseEntity<Object> alterarFormacaoAcademica(@PathVariable UUID idFormacaoAcademica, @RequestBody FormacaoAcademicaRequest formacaoAcademicaRequest) {
        return service.alterarFormacaoAcademica(idFormacaoAcademica, formacaoAcademicaRequest);
    }

    @DeleteMapping("/deletar/{idFormacaoAcademica}")
    public ResponseEntity<Object> deletarFormacaoAcademica(@PathVariable UUID idFormacaoAcademica) {
        return service.deletarFormacaoAcademica(idFormacaoAcademica);
    }
}
