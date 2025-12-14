package com.aqConnecta.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Service
public class AuthService {
    @Value("${spring.application.name}")
    private String applicationName;
    private static final String refreshCookieSuffix = "RefreshToken";

    private final Environment environment;

    @Autowired
    public AuthService(Environment environment) {
        this.environment = environment;
    }

    public String obterNomeDoCookie() {
        return this.applicationName + refreshCookieSuffix;
    }

    public Optional<String> pegarRefreshTokenDosCookies(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies == null) return Optional.empty();

        var nomeDoCookie = this.obterNomeDoCookie();
        for (var cookie : cookies) {
            if (cookie.getName().equals(nomeDoCookie)) {
                return Optional.of(cookie.getValue());
            }
        }

        return Optional.empty();
    }

    public Cookie obterRefreshCookie(String token) {
        var nomeDoCookie = this.obterNomeDoCookie();
        final var umDia = 60 * 60 * 24;
        final var cookie = new Cookie(nomeDoCookie, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(Arrays.asList(this.environment.getActiveProfiles()).contains("prod"));
        cookie.setMaxAge(umDia);
        return cookie;
    }

    public Cookie obterRefreshCookieDeRemocao() {
        final var cookie = new Cookie(this.obterNomeDoCookie(), null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(Arrays.asList(this.environment.getActiveProfiles()).contains("prod"));
        cookie.setMaxAge(0);
        return cookie;
    }
}
