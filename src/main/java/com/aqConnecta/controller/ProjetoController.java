package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.ProjetoRequest;
import com.aqConnecta.model.enums.StatusProjeto;
import com.aqConnecta.service.ProjetoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/projeto")
@RequiredArgsConstructor
@Slf4j
public class ProjetoController {

    private final ProjetoService service;

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrar(@Valid @RequestBody ProjetoRequest request, Authentication authentication) {
        return service.cadastrar(request, authentication.getName());
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listar(
            @RequestParam(value = "titulo", required = false, defaultValue = "") String titulo,
            @RequestParam(value = "idArea", required = false) UUID idArea,
            @RequestParam(value = "status", required = false) StatusProjeto status,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return service.listarPublicos(titulo, idArea, status, pageable);
    }

    @GetMapping("/meus")
    public ResponseEntity<Object> listarMeus(Authentication authentication) {
        return service.listarMeus(authentication.getName());
    }

    @GetMapping("/localizar/{idProjeto}")
    public ResponseEntity<Object> localizar(@PathVariable UUID idProjeto, Authentication authentication) {
        return service.localizar(idProjeto, authentication.getName());
    }

    @PutMapping("/alterar/{idProjeto}")
    public ResponseEntity<Object> alterar(@PathVariable UUID idProjeto, @Valid @RequestBody ProjetoRequest request, Authentication authentication) {
        return service.alterar(idProjeto, request, authentication.getName());
    }

    @DeleteMapping("/deletar/{idProjeto}")
    public ResponseEntity<Object> deletar(@PathVariable UUID idProjeto, Authentication authentication) {
        return service.deletar(idProjeto, authentication.getName());
    }

    @GetMapping("/seguidos")
    public ResponseEntity<Object> listarSeguidos(Authentication authentication) {
        return service.listarSeguidos(authentication.getName());
    }

    @PostMapping("/{idProjeto}/seguir")
    public ResponseEntity<Object> seguir(@PathVariable UUID idProjeto, Authentication authentication) {
        return service.seguir(idProjeto, authentication.getName());
    }

    @DeleteMapping("/{idProjeto}/seguir")
    public ResponseEntity<Object> deixarDeSeguir(@PathVariable UUID idProjeto, Authentication authentication) {
        return service.deixarDeSeguir(idProjeto, authentication.getName());
    }

    @PostMapping("/{idProjeto}/capa")
    public ResponseEntity<Object> uploadCapa(@PathVariable UUID idProjeto, @RequestParam("file") MultipartFile file, Authentication authentication) {
        return service.uploadCapa(idProjeto, file, authentication.getName());
    }

    @PostMapping("/{idProjeto}/galeria")
    public ResponseEntity<Object> adicionarImagem(@PathVariable UUID idProjeto, @RequestParam("file") MultipartFile file, Authentication authentication) {
        return service.adicionarImagemGaleria(idProjeto, file, authentication.getName());
    }

    @DeleteMapping("/{idProjeto}/galeria/{idImagem}")
    public ResponseEntity<Object> removerImagem(@PathVariable UUID idProjeto, @PathVariable UUID idImagem, Authentication authentication) {
        return service.removerImagemGaleria(idProjeto, idImagem, authentication.getName());
    }

    @GetMapping("/{idProjeto}/midia/{filename:.+}")
    public ResponseEntity<?> servirMidia(@PathVariable UUID idProjeto, @PathVariable String filename, Authentication authentication) {
        return service.carregarMidia(idProjeto, filename, authentication.getName());
    }
}
