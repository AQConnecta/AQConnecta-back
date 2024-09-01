package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.LoginRequest;
import com.aqConnecta.DTOs.request.RegistroRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.security.JWTUtil;
import com.aqConnecta.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import java.util.UUID;

@RestController
@RequestMapping("/usuario")
@Slf4j
public class UsuarioController {

    @Autowired
    private final UsuarioService service;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    @Autowired
    public UsuarioController(
            UsuarioService service,
            PasswordEncoder passwordEncoder,
            AuthenticationManager manager,
            JWTUtil jwtUtil) {
        this.service = service;
        this.authenticationManager = manager;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/usuario_full")
    public ResponseEntity<Object> teste() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return ResponseHandler.generateResponse("Usuário completo", HttpStatus.OK, service.localizarPorEmail(authentication.getName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{userUrl}")
    public ResponseEntity<Object> localizarPorUrl(@PathVariable String userUrl) {
        return service.localizarPorUrl(userUrl);
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listar(@RequestParam(value = "userUrl", required = false, defaultValue = "") String userUrl) {
        return service.listar(userUrl);
    }

    @PostMapping("/registrar")
    public ResponseEntity<Object> registerUser(@RequestBody RegistroRequest usuario) {
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
    public ResponseEntity<Object> uploadImagemPerfil(@RequestParam("file") MultipartFile file) {
        return service.salvarImagemPerfil(file);
    }

    @PutMapping("/alterar-imagem-perfil")
    public ResponseEntity<Object> alterarImagemPerfil(@RequestParam("file") MultipartFile file) {
        return service.salvarImagemPerfil(file);
    }

    @DeleteMapping("/remover-imagem-perfil")
    public ResponseEntity<Object> removerImagemPerfil() {
        return service.removerImagemPerfil();
    }

    @PostMapping("/anexar-curriculo")
    public ResponseEntity<Object> anexarCurriculo(@RequestParam("file") MultipartFile file) {
        return service.anexarCurriculo(file);
    }

    @DeleteMapping("/remover-curriculo/{idCurriculo}")
    public ResponseEntity<Object> removerCurriculo(@PathVariable Integer idCurriculo) {
        return service.removerCurriculo(idCurriculo);
    }

    @GetMapping("/curriculos")
    public ResponseEntity<Object> listarCurriculos() {
        return service.listarCurriculo();
    }

    @GetMapping("/candidaturas")
    public ResponseEntity<Object> listarCandidaturas() {
        return service.listarCandidaturas();
    }
}
