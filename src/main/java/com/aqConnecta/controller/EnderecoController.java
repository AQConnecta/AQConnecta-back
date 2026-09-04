package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.EnderecoRequest;
import com.aqConnecta.service.EnderecoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/endereco")
@Slf4j
public class EnderecoController {

    private final EnderecoService service;

    @Autowired
    public EnderecoController(EnderecoService service) {
        this.service = service;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarEndereco(@Valid @RequestBody EnderecoRequest enderecoRequest, Authentication authentication) {
        return service.cadastrarEndereco(enderecoRequest, authentication.getName());
    }

    @GetMapping("/listar/{idUsuario}")
    public ResponseEntity<Object> listarEnderecosPorUsuario(@PathVariable UUID idUsuario, Authentication authentication) {
        return service.listarEnderecosPorUsuario(idUsuario, authentication.getName());
    }

    @GetMapping("/localizar/{idEndereco}")
    public ResponseEntity<Object> localizarEndereco(@PathVariable UUID idEndereco, Authentication authentication) {
        return service.localizarEndereco(idEndereco, authentication.getName());
    }

    @PutMapping("/alterar/{idEndereco}")
    public ResponseEntity<Object> alterarEndereco(@PathVariable UUID idEndereco, @Valid @RequestBody EnderecoRequest enderecoRequest, Authentication authentication) {
        return service.alterarEndereco(idEndereco, enderecoRequest, authentication.getName());
    }

    @DeleteMapping("/deletar/{idEndereco}")
    public ResponseEntity<Object> deletarEndereco(@PathVariable UUID idEndereco, Authentication authentication) {
        return service.deletarEndereco(idEndereco, authentication.getName());
    }
}
