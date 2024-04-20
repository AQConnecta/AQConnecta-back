package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.LoginRequest;
import com.aqConnecta.DTOs.request.RegistroRequest;
import com.aqConnecta.DTOs.response.ErrorResponse;
import com.aqConnecta.DTOs.response.LoginResponse;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.security.JWTUtil;
import com.aqConnecta.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
@Slf4j
public class UsuarioController {

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

    @GetMapping("/teste")
    public ResponseEntity<?> teste() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            return ResponseHandler.generateResponse("Sei la", HttpStatus.OK, service.localizarPorEmail(authentication.getName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registerUser(@RequestBody RegistroRequest usuario) {
        return service.saveUsuario(usuario);
    }

    @RequestMapping(value = "/confirma-conta", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> confirmUserAccount(@RequestParam("token") String confirmationToken) {
        try {
            return service.confirmaEmail(confirmationToken);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseHandler.generateResponse("Erro ao confirmar o email", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @RequestMapping(value = "/recuperando", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> recoveryUser(@RequestParam("token") String confirmationToken, @RequestBody LoginRequest recupera) {
        try {
            return service.recuperarSenha(recupera, confirmationToken);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseHandler.generateResponse("Erro ao recuperar o usuário", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/recuperando-senha")
    public ResponseEntity<?> recuperandoUser(@RequestBody LoginRequest email) {
        try {
            return service.recuperarSenha(email);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseHandler.generateResponse("Erro ao recuperar o usuário", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
