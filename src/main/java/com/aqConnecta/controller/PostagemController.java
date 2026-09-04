package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.PostagemRequest;
import com.aqConnecta.service.PostagemService;
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
@RequestMapping("/projeto/{idProjeto}/posts")
@RequiredArgsConstructor
@Slf4j
public class PostagemController {

    private final PostagemService service;

    @PostMapping
    public ResponseEntity<Object> criar(@PathVariable UUID idProjeto, @Valid @RequestBody PostagemRequest request, Authentication authentication) {
        return service.criar(idProjeto, request, authentication.getName());
    }

    @GetMapping
    public ResponseEntity<Object> listar(
            @PathVariable UUID idProjeto,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size);
        return service.listar(idProjeto, authentication.getName(), pageable);
    }

    @GetMapping("/{idPost}")
    public ResponseEntity<Object> localizar(@PathVariable UUID idProjeto, @PathVariable UUID idPost, Authentication authentication) {
        return service.localizar(idProjeto, idPost, authentication.getName());
    }

    @PutMapping("/{idPost}")
    public ResponseEntity<Object> alterar(@PathVariable UUID idProjeto, @PathVariable UUID idPost, @Valid @RequestBody PostagemRequest request, Authentication authentication) {
        return service.alterar(idProjeto, idPost, request, authentication.getName());
    }

    @DeleteMapping("/{idPost}")
    public ResponseEntity<Object> deletar(@PathVariable UUID idProjeto, @PathVariable UUID idPost, Authentication authentication) {
        return service.deletar(idProjeto, idPost, authentication.getName());
    }

    @PostMapping("/{idPost}/imagens")
    public ResponseEntity<Object> adicionarImagem(@PathVariable UUID idProjeto, @PathVariable UUID idPost, @RequestParam("file") MultipartFile file, Authentication authentication) {
        return service.adicionarImagem(idProjeto, idPost, file, authentication.getName());
    }

    @DeleteMapping("/{idPost}/imagens/{idImagem}")
    public ResponseEntity<Object> removerImagem(@PathVariable UUID idProjeto, @PathVariable UUID idPost, @PathVariable UUID idImagem, Authentication authentication) {
        return service.removerImagem(idProjeto, idPost, idImagem, authentication.getName());
    }

    @GetMapping("/{idPost}/midia/{filename:.+}")
    public ResponseEntity<?> servirMidia(@PathVariable UUID idProjeto, @PathVariable UUID idPost, @PathVariable String filename, Authentication authentication) {
        return service.carregarMidia(idProjeto, idPost, filename, authentication.getName());
    }
}
