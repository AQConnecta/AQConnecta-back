package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.LoginRequest;
import com.aqConnecta.DTOs.request.RegistroRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/usuario")
@Slf4j
public class UsuarioController {

    private final UsuarioService service;

    @Autowired
    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/usuario_full")
    public ResponseEntity<Object> teste(Authentication authentication) {
        try {
            return ResponseHandler.generateResponse("Usuário completo", HttpStatus.OK,
                service.localizarPorEmail(authentication.getName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{userUrl}")
    public ResponseEntity<Object> localizarPorUrl(@PathVariable String userUrl, Authentication authentication) {
        return service.localizarPorUrl(userUrl, authentication.getName());
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listar(
            @RequestParam(value = "userUrl", required = false, defaultValue = "") String userUrl,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size);
        return service.listar(userUrl, pageable);
    }

    @PostMapping("/registrar")
    public ResponseEntity<Object> registerUser(@Valid @RequestBody RegistroRequest usuario) {
        return service.saveUsuario(usuario);
    }

    @RequestMapping(value = "/confirma-conta", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Object> confirmUserAccount(@RequestParam("token") String confirmationToken) {
        try {
            return service.confirmaEmail(confirmationToken);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseHandler.generateResponse("Erro ao confirmar o email", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @RequestMapping(value = "/recuperando", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Object> recoveryUser(@RequestParam("token") String confirmationToken, @RequestBody LoginRequest recupera) {
        try {
            return service.recuperarSenha(recupera, confirmationToken);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseHandler.generateResponse("Erro ao recuperar o usuário", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/recuperando-senha")
    public ResponseEntity<Object> recuperandoUser(@RequestBody LoginRequest email) {
        try {
            return service.recuperarSenha(email);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseHandler.generateResponse("Erro ao recuperar o usuário", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/upload-imagem-perfil")
    public ResponseEntity<Object> uploadImagemPerfil(@RequestParam("file") MultipartFile file, Authentication authentication) {
        return service.salvarImagemPerfil(file, authentication.getName());
    }

    @PutMapping("/alterar-imagem-perfil")
    public ResponseEntity<Object> alterarImagemPerfil(@RequestParam("file") MultipartFile file, Authentication authentication) {
        return service.salvarImagemPerfil(file, authentication.getName());
    }

    @DeleteMapping("/remover-imagem-perfil")
    public ResponseEntity<Object> removerImagemPerfil(Authentication authentication) {
        return service.removerImagemPerfil(authentication.getName());
    }

    @PostMapping("/anexar-curriculo")
    public ResponseEntity<Object> anexarCurriculo(@RequestParam("file") MultipartFile file, @RequestParam("nome") String nome, Authentication authentication) {
        return service.anexarCurriculo(file, nome, authentication.getName());
    }

    @DeleteMapping("/remover-curriculo/{idCurriculo}")
    public ResponseEntity<Object> removerCurriculo(@PathVariable Integer idCurriculo, Authentication authentication) {
        return service.removerCurriculo(idCurriculo, authentication.getName());
    }

    @DeleteMapping("/inativar-usuario/{idUsuario}")
    public ResponseEntity<Object> inativarUsuario(@PathVariable UUID idUsuario) {
        return service.inativarUsuario(idUsuario);
    }

    @PutMapping("/reativar-usuario/{idUsuario}")
    public ResponseEntity<Object> reativarUsuario(@PathVariable UUID idUsuario) {
        return service.reativarUsuario(idUsuario);
    }

    @GetMapping("/curriculos")
    public ResponseEntity<Object> listarCurriculos(Authentication authentication) {
        return service.listarCurriculo(authentication.getName());
    }

    @GetMapping("/candidaturas")
    public ResponseEntity<Object> listarCandidaturas(Authentication authentication) {
        return service.listarCandidaturas(authentication.getName());
    }
}
