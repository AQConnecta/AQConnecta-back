package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.LoginRequest;
import com.aqConnecta.DTOs.request.RegistroRequest;
import com.aqConnecta.DTOs.response.ErrorResponse;
import com.aqConnecta.DTOs.response.LoginResponse;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.security.JWTUtil;
import com.aqConnecta.service.UsuarioService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final UsuarioService service;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    @Autowired
    public AuthController(
            UsuarioService service,
            PasswordEncoder passwordEncoder,
            AuthenticationManager manager,
            JWTUtil jwtUtil) {
        this.service = service;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = manager;
        this.jwtUtil = jwtUtil;
    }

    @ResponseBody
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            System.out.println(request.getEmail() + "\n" + request.getSenha());
            Authentication authentication =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha()));
            Usuario usuario = service.localizarPorEmail(authentication.getName());
            LoginResponse response = new LoginResponse(usuario, jwtUtil.generateToken(usuario.getEmail()));
            log.info("Usuário {} logou no sistema", response.getUsuario().getEmail());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.error("Usuário {} tentou logar com credenciais inválidas", request.getEmail());
            ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST, "Usuário ou senha incorretos");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registerUser(@RequestBody RegistroRequest usuario) {
        return service.saveUsuario(usuario);

    }

    @RequestMapping(value="/confirma-conta", method= {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> confirmUserAccount(@RequestParam("token")String confirmationToken) {
        try {
            return service.confirmaEmail(confirmationToken);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @RequestMapping(value="/recuperando", method= {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> recoveryUser(@RequestParam("token")String confirmationToken, @RequestBody LoginRequest recupera) {
        try {
            return service.recuperarSenha(recupera, confirmationToken);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/recuperando-senha")
    public ResponseEntity<?> recuperandoUser(@RequestBody LoginRequest email) {
        try {
            return service.recuperarSenha(email);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
