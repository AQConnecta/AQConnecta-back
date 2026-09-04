package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.LoginRequest;
import com.aqConnecta.DTOs.request.RegistroRequest;
import com.aqConnecta.DTOs.response.LoginResponse;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.security.JWTUtil;
import com.aqConnecta.security.RefreshTokenClaims;
import com.aqConnecta.service.AuthService;
import com.aqConnecta.service.EmailConfirmacaoPageRenderer;
import com.aqConnecta.service.UsuarioService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private final EmailConfirmacaoPageRenderer pageRenderer;
    private final com.aqConnecta.service.BusinessMetrics businessMetrics;

    @Autowired
    public AuthController(
        UsuarioService service,
        AuthenticationManager manager,
        JWTUtil jwtUtil,
        AuthService authService,
        EmailConfirmacaoPageRenderer pageRenderer,
        com.aqConnecta.service.BusinessMetrics businessMetrics
    ) {
        this.service = service;
        this.authService = authService;
        this.authenticationManager = manager;
        this.jwtUtil = jwtUtil;
        this.pageRenderer = pageRenderer;
        this.businessMetrics = businessMetrics;
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
        } catch (Exception e) {
            log.error("Usuário tentou logar com um token contendo claims inválidas (email: {}, id: {}): {}",
                claims.email(), claims.id(), e.getMessage());
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse httpResponse) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getSenha()));
            Usuario usuario = service.localizarPorEmail(request.getEmail());

            log.info("Usuário {} logou no sistema", usuario.getEmail());
            businessMetrics.login();
            LoginResponse response = new LoginResponse(usuario, jwtUtil.generateToken(usuario.getEmail()));
            var refreshToken = this.jwtUtil.generateRefreshToken(usuario.getId(), usuario.getEmail());
            var refreshTokenCookie = this.authService.obterRefreshCookie(refreshToken);

            httpResponse.addCookie(refreshTokenCookie);
            return ResponseHandler.generateResponse(null, HttpStatus.OK, response);
        } catch (BadCredentialsException e) {
            log.error("Usuário {} tentou logar com credenciais inválidas", request.getEmail());
            return ResponseHandler.generateResponse("Usuário ou senha incorretos", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistroRequest usuario) {
        return service.saveUsuario(usuario);
    }

    @RequestMapping(
        value = "/confirma-conta",
        method = {RequestMethod.GET, RequestMethod.POST},
        produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> confirmUserAccount(@RequestParam("token") String confirmationToken) {
        try {
            UsuarioService.ConfirmacaoEmailResult result = service.confirmaEmailParaPagina(confirmationToken);
            String html = switch (result.status()) {
                case SUCESSO   -> pageRenderer.renderSuccess();
                case EXPIRADO  -> pageRenderer.renderExpired();
                case INVALIDO  -> pageRenderer.renderError(result.mensagem());
            };
            HttpStatus status = result.status() == UsuarioService.ConfirmacaoStatus.SUCESSO
                ? HttpStatus.OK
                : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
        } catch (Exception e) {
            log.error("Erro inesperado ao confirmar e-mail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_HTML)
                .body(pageRenderer.renderError("Erro inesperado. Tente novamente em instantes."));
        }
    }

    @RequestMapping(value = "/recuperando", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> recoveryUser(@RequestParam("token") String confirmationToken,
        @RequestBody LoginRequest recupera) {
        try {
            return service.recuperarSenha(recupera, confirmationToken);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseHandler.generateResponse("Erro ao recuperar o usuário", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/reenviar-confirmacao")
    public ResponseEntity<?> reenviarConfirmacao(@RequestBody LoginRequest request) {
        return service.reenviarConfirmacao(request.getEmail());
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
