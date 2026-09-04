package com.aqConnecta.config;

import com.aqConnecta.security.JWTAuthenticationFilter;
import com.aqConnecta.security.JWTAuthorizationFilter;
import com.aqConnecta.security.JWTUtil;
import com.aqConnecta.security.RateLimitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${cors.urls:*}")
    private String corsUrls;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration configuration = new CorsConfiguration();

            if (corsUrls.equals("*")) {
                configuration.setAllowedOriginPatterns(List.of("*"));
            } else {
                configuration.setAllowedOrigins(Arrays.asList(corsUrls.split(",")));
            }

            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
            configuration.setAllowedHeaders(Arrays.asList("*"));
            configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
            configuration.setAllowCredentials(!corsUrls.equals("*"));
            configuration.setMaxAge(3600L);

            return configuration;
        }));

        http.addFilterBefore(new RateLimitFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilter(new JWTAuthenticationFilter(authenticationManager, jwtUtil));
        http.addFilter(new JWTAuthorizationFilter(authenticationManager, jwtUtil, userDetailsService));

        http.authorizeHttpRequests(requests -> requests
            // CORS preflight
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

            // Endpoints públicos - autenticação
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers("/usuario/registrar").permitAll()
            .requestMatchers("/usuario/confirma-conta").permitAll()
            .requestMatchers("/usuario/recuperando").permitAll()
            .requestMatchers("/usuario/recuperando-senha").permitAll()

            // Endpoints públicos - leitura
            .requestMatchers(HttpMethod.GET, "/competencia/listar").permitAll()
            .requestMatchers(HttpMethod.GET, "/competencia/competencias_quentes").permitAll()
            .requestMatchers(HttpMethod.GET, "/area/listar").permitAll()

            // Leitura pública de vagas e projetos (navegação sem login)
            .requestMatchers(HttpMethod.GET, "/vaga/listar").permitAll()
            .requestMatchers(HttpMethod.GET, "/vaga/listar/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/vaga/localizar/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/vaga/por-projeto/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/projeto/listar").permitAll()
            .requestMatchers(HttpMethod.GET, "/projeto/localizar/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/projeto/*/midia/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/projeto/*/posts").permitAll()
            .requestMatchers(HttpMethod.GET, "/projeto/*/posts/**").permitAll()

            // Arquivos servidos do PVC (fotos de perfil, currículos, diplomas, etc.)
            .requestMatchers(HttpMethod.GET, "/files/**").permitAll()

            // Actuator
            .requestMatchers("/actuator/health/**").permitAll()
            .requestMatchers("/actuator/info").permitAll()
            .requestMatchers("/actuator/prometheus").permitAll()

            // Endpoints exclusivos de admin
            .requestMatchers(HttpMethod.POST, "/competencia/cadastrar").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/competencia/alterar/**").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/competencia/deletar/**").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.GET, "/competencia/pendentes").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/competencia/aprovar/**").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/competencia/recusar/**").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.POST, "/universidade/cadastrar").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/universidade/alterar/**").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/universidade/deletar/**").hasAuthority("ADMIN")
            .requestMatchers("/usuario/inativar-usuario/**").hasAuthority("ADMIN")
            .requestMatchers("/usuario/reativar-usuario/**").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.POST, "/area/cadastrar").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/area/alterar/**").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/area/deletar/**").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.GET, "/denuncia/listar").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/denuncia/alterar/**").hasAuthority("ADMIN")

            // Todo o resto requer autenticação
            .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
    throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
