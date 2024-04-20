package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.EnderecoRequest;
import com.aqConnecta.service.EnderecoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Object> cadastrarEndereco(@RequestBody EnderecoRequest enderecoRequest) {
        return service.cadastrarEndereco(enderecoRequest);
    }

    @GetMapping("/listar/{idUsuario}")
    public ResponseEntity<Object> listarEnderecosPorUsuario(@PathVariable UUID idUsuario) {
        return service.listarEnderecosPorUsuario(idUsuario);
    }

    @GetMapping("/localizar/{idEndereco}")
    public ResponseEntity<Object> localizarEndereco(@PathVariable UUID idEndereco) {
        return service.localizarEndereco(idEndereco);
    }

    @PutMapping("/alterar/{idEndereco}")
    public ResponseEntity<Object> alterarEndereco(@PathVariable UUID idEndereco, @RequestBody EnderecoRequest enderecoRequest) {
        return service.alterarEndereco(idEndereco, enderecoRequest);
    }

    @DeleteMapping("/deletar/{idEndereco}")
    public ResponseEntity<Object> deletarEndereco(@PathVariable UUID idEndereco) {
        return service.deletarEndereco(idEndereco);
    }
}
