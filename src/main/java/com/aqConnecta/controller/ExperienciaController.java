package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.CadastroExperienciaRequest;
import com.aqConnecta.DTOs.request.LoginRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.service.ExperienciaService;
import com.aqConnecta.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/experiencia")
@Slf4j
public class ExperienciaController {

    private final ExperienciaService service;

    @Autowired
    public ExperienciaController(ExperienciaService service) {
        this.service = service;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarExperiencia(@RequestBody CadastroExperienciaRequest experienciaRequest) {
        return service.cadastrarExperiencia(experienciaRequest);
    }

    @GetMapping("/listar/{idUsuario}")
    public ResponseEntity<Object> listarExperienciasPorUsuario(@PathVariable UUID idUsuario) {
        return service.listarExperienciasPorUsuario(idUsuario);
    }


    @GetMapping("/localizar/{idExperiencia}")
    public ResponseEntity<Object> localizarExperiencia(@PathVariable UUID idExperiencia) {
        return service.localizarExperiencia(idExperiencia);
    }
    @PutMapping("/alterar/{idExperiencia}")
    public ResponseEntity<Object> alterarExperiencia(@PathVariable UUID idExperiencia, @RequestBody CadastroExperienciaRequest experienciaRequest) {
        return service.alterarExperiencia(experienciaRequest);
    }

    @DeleteMapping("/deletar/{idExperiencia}")
    public ResponseEntity<Object> deletarExperiencia(@PathVariable UUID idExperiencia) {
        return service.deletarExperiencia(idExperiencia);
    }

//    @PostMapping("/editar")
//    public ResponseEntity<Object> recuperandoUser(@RequestBody LoginRequest email) {
//        try {
//            return service.recuperarSenha(email);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            return ResponseHandler.generateResponse("Erro ao recuperar o usuário", HttpStatus.BAD_REQUEST, e.getMessage());
//        }
//    }
//
//    @PostMapping("/deletar")
//    public ResponseEntity<Object> recuperandoUser(@RequestBody LoginRequest email) {
//        try {
//            return service.recuperarSenha(email);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            return ResponseHandler.generateResponse("Erro ao recuperar o usuário", HttpStatus.BAD_REQUEST, e.getMessage());
//        }
//    }

}
