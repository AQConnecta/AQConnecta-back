package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.AlterarStatusDenunciaRequest;
import com.aqConnecta.DTOs.request.DenunciaRequest;
import com.aqConnecta.model.enums.StatusDenuncia;
import com.aqConnecta.service.DenunciaService;
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
@RequestMapping("/denuncia")
@RequiredArgsConstructor
@Slf4j
public class DenunciaController {

    private final DenunciaService service;

    @PostMapping("/projeto/{idProjeto}")
    public ResponseEntity<Object> criar(@PathVariable UUID idProjeto, @Valid @RequestBody DenunciaRequest request, Authentication authentication) {
        return service.criar(idProjeto, request, authentication.getName());
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listar(
            @RequestParam(value = "status", required = false) StatusDenuncia status,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return service.listar(status, pageable);
    }

    @PutMapping("/alterar/{idDenuncia}")
    public ResponseEntity<Object> alterarStatus(@PathVariable UUID idDenuncia, @Valid @RequestBody AlterarStatusDenunciaRequest request, Authentication authentication) {
        return service.alterarStatus(idDenuncia, request, authentication.getName());
    }
}
