package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.AreaRequest;
import com.aqConnecta.service.AreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/area")
@RequiredArgsConstructor
@Slf4j
public class AreaController {

    private final AreaService service;

    @GetMapping("/listar")
    public ResponseEntity<Object> listar(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "100") int size) {
        return service.listar(search, page, size);
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrar(@Valid @RequestBody AreaRequest request) {
        return service.cadastrar(request);
    }

    @PutMapping("/alterar/{idArea}")
    public ResponseEntity<Object> alterar(@PathVariable UUID idArea, @Valid @RequestBody AreaRequest request) {
        return service.alterar(idArea, request);
    }

    @DeleteMapping("/deletar/{idArea}")
    public ResponseEntity<Object> deletar(@PathVariable UUID idArea) {
        return service.deletar(idArea);
    }
}
