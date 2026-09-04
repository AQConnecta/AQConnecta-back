package com.aqConnecta.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Renderiza a página HTML retornada após o clique no link de confirmação de e-mail.
 * Carrega o template uma única vez no startup e faz substituição de placeholders.
 */
@Slf4j
@Component
public class EmailConfirmacaoPageRenderer {

    private static final String TEMPLATE_PATH = "templates/email-confirmacao.html";

    private static final String ICON_SUCCESS =
            "<svg viewBox='0 0 24 24'><path d='M20 6 9 17l-5-5'/></svg>";
    private static final String ICON_ERROR =
            "<svg viewBox='0 0 24 24'><path d='M18 6 6 18M6 6l12 12'/></svg>";
    private static final String ICON_EXPIRED =
            "<svg viewBox='0 0 24 24'><path d='M12 8v4l3 2M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z'/></svg>";

    @Value("${app.frontend-url:https://aqconnecta.cm.utfpr.edu.br}")
    private String frontendUrl;

    private String template;

    @PostConstruct
    void load() { 
        try (InputStream in = new ClassPathResource(TEMPLATE_PATH).getInputStream()) {
            this.template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Falha ao carregar template {}", TEMPLATE_PATH, e);
            this.template = "<html><body><h1>{{TITLE}}</h1><p>{{MESSAGE}}</p></body></html>";
        }
    }

    public String renderSuccess() {
        return render(Map.of(
                "TITLE", "E-mail confirmado!",
                "MESSAGE", "Sua conta foi ativada com sucesso. Agora você já pode entrar e começar a usar o AQConnecta.",
                "ICON_SVG", ICON_SUCCESS,
                "ICON_BG", "#dcfce7",
                "ICON_COLOR", "#16a34a",
                "ACTION_URL", frontendUrl + "/login",
                "ACTION_LABEL", "Fazer login"
        ));
    }

    public String renderExpired() {
        return render(Map.of(
                "TITLE", "Link expirado",
                "MESSAGE", "Este link de confirmação não é mais válido. Solicite um novo cadastro ou peça reenvio do e-mail.",
                "ICON_SVG", ICON_EXPIRED,
                "ICON_BG", "#fef3c7",
                "ICON_COLOR", "#d97706",
                "ACTION_URL", frontendUrl + "/register",
                "ACTION_LABEL", "Voltar ao cadastro"
        ));
    }

    public String renderError(String mensagem) {
        return render(Map.of(
                "TITLE", "Não foi possível confirmar",
                "MESSAGE", mensagem == null || mensagem.isBlank()
                        ? "Ocorreu um erro ao confirmar seu e-mail. Tente novamente em instantes."
                        : mensagem,
                "ICON_SVG", ICON_ERROR,
                "ICON_BG", "#fee2e2",
                "ICON_COLOR", "#dc2626",
                "ACTION_URL", frontendUrl + "/login",
                "ACTION_LABEL", "Ir para o login"
        ));
    }

    private String render(Map<String, String> values) {
        String html = template;
        for (Map.Entry<String, String> e : values.entrySet()) {
            html = html.replace("{{" + e.getKey() + "}}", e.getValue());
        }
        return html;
    }
}
