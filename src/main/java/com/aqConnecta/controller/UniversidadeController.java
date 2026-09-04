package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.UniversidadeRequest;
import com.aqConnecta.service.UniversidadeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/universidade")
@Slf4j
public class UniversidadeController {

    private final UniversidadeService service;

    @Autowired
    public UniversidadeController(UniversidadeService service) {
        this.service = service;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarUniversidade(@Valid @RequestBody UniversidadeRequest universidadeRequest) {
        return service.cadastrarUniversidade(universidadeRequest);
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listarUniversidades(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return service.listarUniversidades(search, pageable);
    }

    @GetMapping("/localizar/{idUniversidade}")
    public ResponseEntity<Object> localizarUniversidade(@PathVariable UUID idUniversidade) {
        return service.localizarUniversidade(idUniversidade);
    }

    @PutMapping("/alterar/{idUniversidade}")
    public ResponseEntity<Object> alterarUniversidade(@PathVariable UUID idUniversidade, @Valid @RequestBody UniversidadeRequest universidadeRequest) {
        return service.alterarUniversidade(idUniversidade, universidadeRequest);
    }

    @DeleteMapping("/deletar/{idUniversidade}")
    public ResponseEntity<Object> deletarUniversidade(@PathVariable UUID idUniversidade) {
        return service.deletarUniversidade(idUniversidade);
    }
}
