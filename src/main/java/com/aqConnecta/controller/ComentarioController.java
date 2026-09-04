package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.ComentarioRequest;
import com.aqConnecta.service.ComentarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projeto/{idProjeto}/posts/{idPost}/comentarios")
@RequiredArgsConstructor
@Slf4j
public class ComentarioController {

    private final ComentarioService service;

    @GetMapping
    public ResponseEntity<Object> listar(
            @PathVariable UUID idProjeto,
            @PathVariable UUID idPost,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size);
        return service.listar(idProjeto, idPost, authentication.getName(), pageable);
    }

    @PostMapping
    public ResponseEntity<Object> criar(@PathVariable UUID idProjeto, @PathVariable UUID idPost, @Valid @RequestBody ComentarioRequest request, Authentication authentication) {
        return service.criar(idProjeto, idPost, request, authentication.getName());
    }

    @DeleteMapping("/{idComentario}")
    public ResponseEntity<Object> deletar(@PathVariable UUID idProjeto, @PathVariable UUID idPost, @PathVariable UUID idComentario, Authentication authentication) {
        return service.deletar(idProjeto, idPost, idComentario, authentication.getName());
    }
}
