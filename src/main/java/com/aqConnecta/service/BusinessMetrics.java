package com.aqConnecta.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    private final Counter usuariosCadastrados;
    private final Counter emailsEnviados;
    private final Counter emailsFalhados;
    private final Counter projetosCriados;
    private final Counter vagasCriadas;
    private final Counter logins;

    public BusinessMetrics(MeterRegistry registry) {
        this.usuariosCadastrados = Counter.builder("aqconnecta_usuarios_cadastrados_total")
            .description("Total de usuários cadastrados").register(registry);
        this.emailsEnviados = Counter.builder("aqconnecta_emails_total")
            .tag("status", "enviado").description("Total de e-mails processados").register(registry);
        this.emailsFalhados = Counter.builder("aqconnecta_emails_total")
            .tag("status", "falha").description("Total de e-mails processados").register(registry);
        this.projetosCriados = Counter.builder("aqconnecta_projetos_criados_total")
            .description("Total de projetos criados").register(registry);
        this.vagasCriadas = Counter.builder("aqconnecta_vagas_criadas_total")
            .description("Total de vagas criadas").register(registry);
        this.logins = Counter.builder("aqconnecta_logins_total")
            .description("Total de logins bem-sucedidos").register(registry);
    }

    public void usuarioCadastrado() {
        usuariosCadastrados.increment();
    }

    public void emailEnviado() {
        emailsEnviados.increment();
    }

    public void emailFalhou() {
        emailsFalhados.increment();
    }

    public void projetoCriado() {
        projetosCriados.increment();
    }

    public void vagaCriada() {
        vagasCriadas.increment();
    }

    public void login() {
        logins.increment();
    }
}
