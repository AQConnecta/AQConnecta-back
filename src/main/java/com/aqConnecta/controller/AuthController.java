package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.LoginRequest;
import com.aqConnecta.DTOs.request.RegistroRequest;
import com.aqConnecta.DTOs.response.LoginResponse;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.security.JWTUtil;
import com.aqConnecta.security.RefreshTokenClaims;
import com.aqConnecta.service.AuthService;
import com.aqConnecta.service.UsuarioService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.function.Function;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final UsuarioService service;
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    @Autowired
    public AuthController(
        UsuarioService service,
        AuthenticationManager manager,
        JWTUtil jwtUtil,
        AuthService authService
    ) {
        this.service = service;
        this.authService = authService;
        this.authenticationManager = manager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        var cookie = this.authService.obterRefreshCookieDeRemocao();
        response.addCookie(cookie);
    }

    @ResponseBody
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        Function<String, ResponseEntity<?>> retornarErroDeAutorizacao = (String message) -> {
            response.addCookie(this.authService.obterRefreshCookieDeRemocao());
            return ResponseHandler.generateResponse(message, HttpStatus.UNAUTHORIZED);
        };

        var cookie = this.authService.pegarRefreshTokenDosCookies(request);
        if (cookie.isEmpty()) {
            return retornarErroDeAutorizacao.apply("Não foi possível reautenticar.");
        }

        String cookieValue = cookie.get();

        Optional<Claims> _claims = this.jwtUtil.getTokenValidatedClaims(cookieValue);
        if (_claims.isEmpty() || !this.jwtUtil.refreshTokenValido(_claims.get())) {
            return retornarErroDeAutorizacao.apply("Token inválido.");
        }

        RefreshTokenClaims claims = this.jwtUtil.montarRefreshTokenClaims(_claims.get());
        Usuario usuario = null;
        try {
            usuario = this.service.localizar(claims.id());
        }
        catch (Exception e) {
            log.error("Usuário tentou logar com um token contendo claims inválidas (email: {}, id: {}): {}",
                claims.email(),
                claims.id(),
                e.getMessage()
            );

            return retornarErroDeAutorizacao.apply("Não foi possível reautenticar.");
        }

        log.info("Reautenticando usuário {} no sistema", usuario.getEmail());
        var loginResponse = new LoginResponse(usuario, this.jwtUtil.generateToken(usuario.getEmail()));
        var novoRefreshCookie =
            this.authService.obterRefreshCookie(this.jwtUtil.generateRefreshToken(usuario.getId(), usuario.getEmail()));

        response.addCookie(novoRefreshCookie);
        return ResponseHandler.generateResponse(null, HttpStatus.OK, loginResponse);
    }

    @ResponseBody
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse httpResponse) {
        try {
            Usuario usuario = service.localizarPorEmail(request.getEmail());
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getSenha()));

            log.info("Usuário {} logou no sistema", usuario.getEmail());
            LoginResponse response = new LoginResponse(usuario, jwtUtil.generateToken(usuario.getEmail()));
            var refreshToken = this.jwtUtil.generateRefreshToken(usuario.getId(), usuario.getEmail());
            var refreshTokenCookie = this.authService.obterRefreshCookie(refreshToken);

            httpResponse.addCookie(refreshTokenCookie);
            return ResponseHandler.generateResponse(null, HttpStatus.OK, response);
        }
        catch (BadCredentialsException e) {
            log.error("Usuário {} tentou logar com credenciais inválidas", request.getEmail());
            return ResponseHandler.generateResponse("Usuário ou senha incorretos", HttpStatus.BAD_REQUEST);
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
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return ResponseHandler.generateResponse("Erro ao confirmar o email",
                HttpStatus.BAD_REQUEST,
                e.getMessage());
        }
    }

    @RequestMapping(value = "/recuperando", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> recoveryUser(@RequestParam("token") String confirmationToken,
        @RequestBody LoginRequest recupera) {
        try {
            return service.recuperarSenha(recupera, confirmationToken);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return ResponseHandler.generateResponse("Erro ao recuperar o usuário",
                HttpStatus.BAD_REQUEST,
                e.getMessage());
        }
    }

    @PostMapping("/recuperando-senha")
    public ResponseEntity<?> recuperandoUser(@RequestBody LoginRequest email) {
        try {
            return service.recuperarSenha(email);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return ResponseHandler.generateResponse("Erro ao recuperar o usuário",
                HttpStatus.BAD_REQUEST,
                e.getMessage());
        }
    }

}
