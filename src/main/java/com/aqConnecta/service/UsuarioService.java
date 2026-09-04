package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.LoginRequest;
import com.aqConnecta.DTOs.request.RegistroRequest;
import com.aqConnecta.DTOs.response.MeuUsuarioResponse;
import com.aqConnecta.DTOs.response.OutroUsuarioResponse;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.exception.RecursoNaoEncontradoException;
import com.aqConnecta.exception.usuarios.UsuarioNaoVerificadoException;
import com.aqConnecta.exception.usuarios.UsuarioRemovidoException;
import com.aqConnecta.model.*;
import com.aqConnecta.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UsuarioService {

    private static final long TOKEN_EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 horas

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConfirmaRepository confirmaRepository;

    @Autowired
    private PermissaoRepository permissaoRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DocumentoService documentoService;

    @Autowired
    private BusinessMetrics businessMetrics;

    /**
     * Base pública do backend, usada para construir links que vão para o e-mail.
     * Aceita formatos com ou sem protocolo. Exemplos válidos:
     *   APP_URL=aqconnecta-back.riume.com.br
     *   APP_URL=https://aqconnecta-back.riume.com.br
     *   APP_URL=http://localhost:8080   (para dev)
     */
    @Value("${url}")
    private String url;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private CandidaturaRepository candidaturaRepository;

    @Autowired
    private CurriculoRepository curriculoRepository;

    public ResponseEntity<Object> saveUsuario(RegistroRequest registro) throws RuntimeException {
        if (usuarioRepository.existsByEmail(registro.getEmail())) {
            return ResponseHandler.generateResponse("Erro: Email já está em uso!", HttpStatus.CONFLICT, null);
        }

        Set<Permissao> permissoes = new HashSet<>();
        permissoes.add(permissaoRepository.findById(1L)
            .orElseThrow(() -> new RuntimeException(
                "Erro interno, não foi possivel criar conta com permissão de cliente")));

        Usuario usuario = Usuario.builder()
            .nome(registro.getNome())
            .email(registro.getEmail())
            .senha(encoder.encode(registro.getSenha()))
            .permissao(permissoes)
            .userUrl(generateUserUrl(registro.getNome()))
            .build();

        // saveAndFlush força o INSERT do usuário imediatamente.
        // Sem isso, o @GeneratedValue(UUID) + ordem de flush do Hibernate pode
        // tentar inserir o ConfirmaToken (abaixo) antes do Usuario, violando a FK.
        usuario = usuarioRepository.saveAndFlush(usuario);
        businessMetrics.usuarioCadastrado();

        enviarEmailConfirmacao(usuario);

        return ResponseHandler.generateResponse("Verifique seu e-mail", HttpStatus.OK, usuario);
    }

    private void enviarEmailConfirmacao(Usuario usuario) {
        long now = System.currentTimeMillis();
        ConfirmaToken confirmationToken = ConfirmaToken.builder()
            .token(UUID.randomUUID().toString())
            .usuario(usuario)
            .dataCriacao(new Timestamp(now))
            .dataExpiracao(new Timestamp(now + TOKEN_EXPIRATION_MS))
            .build();

        confirmaRepository.save(confirmationToken);

        String subject = "Complete a inscrição!";
        String text = "Para confirmar a conta, por favor clique aqui :";
        String link = buildBaseUrl() + "/api/auth/confirma-conta?token=" + confirmationToken.getToken();
        String corpoEmail = emailService.criarCorpoEmail(usuario.getNome(), text, link);
        emailService.sendEmail(usuario.getEmail(), subject, corpoEmail);
    }

    public ResponseEntity<Object> reenviarConfirmacao(String email) {
        try {
            if (email == null || email.isBlank()) {
                return ResponseHandler.generateResponse("E-mail é obrigatório.", HttpStatus.BAD_REQUEST);
            }
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim());
            if (usuario != null && !Boolean.TRUE.equals(usuario.getAtivado())) {
                enviarEmailConfirmacao(usuario);
            }
            return ResponseHandler.generateResponse(
                "Se houver uma conta não ativada para este e-mail, enviamos um novo link de confirmação.",
                HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erro ao reenviar confirmação de e-mail", e);
            return ResponseHandler.generateResponse("Não foi possível reenviar o e-mail. Tente novamente.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateUserUrl(String nome) {
        if (nome == null) {
            return null;
        }
        Random random = new Random();
        String normalized = Normalizer.normalize(nome, Normalizer.Form.NFD);
        normalized = Pattern.compile("\\p{M}").matcher(normalized).replaceAll("");
        normalized = normalized.toLowerCase().replace(" ", "-");
        if (usuarioRepository.existsByUserUrl(normalized)) {
            normalized = normalized.concat(String.format("-%d", Math.abs(random.nextLong())));
        }
        return normalized;
    }

    public ResponseEntity<Object> confirmaEmail(String confirmaToken) throws Exception {
        ConfirmaToken token =
            confirmaRepository.findByToken(confirmaToken).orElseThrow(() -> new Exception("Token não encontrado"));

        if (token.isExpirado()) {
            confirmaRepository.delete(token);
            return ResponseHandler.generateResponse("Token expirado. Solicite um novo.", HttpStatus.BAD_REQUEST);
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(token.getUsuario().getEmail());
        usuario.setAtivado(true);
        usuarioRepository.save(usuario);
        confirmaRepository.delete(token);
        return ResponseHandler.generateResponse("Email verificado com sucesso!", HttpStatus.OK, null);
    }

    public enum ConfirmacaoStatus { SUCESSO, EXPIRADO, INVALIDO }

    public record ConfirmacaoEmailResult(ConfirmacaoStatus status, String mensagem) {}

    /**
     * Variante que devolve um result-type em vez de ResponseEntity, para o controller
     * conseguir escolher entre renderizar JSON ou HTML.
     *
     * Regras de negócio:
     *   - Se o usuário (identificado pelo token) já está ATIVADO → SUCESSO,
     *     independente do estado do token. Acessar o link de novo é idempotente.
     *   - Se o token está EXPIRADO e o usuário NÃO está ativado → EXPIRADO.
     *   - Se o token é válido e o usuário não está ativado → ativa e retorna SUCESSO.
     *   - Se o token não existe no banco → assumimos SUCESSO (já foi usado e limpo
     *     em alguma rotina anterior; a UX "se você chegou aqui, está validado").
     *
     * Os tokens NÃO são mais deletados nesta operação — assim cliques repetidos
     * continuam retornando uma resposta coerente para o usuário.
     */
    public ConfirmacaoEmailResult confirmaEmailParaPagina(String confirmaToken) {
        Optional<ConfirmaToken> opt = confirmaRepository.findByToken(confirmaToken);

        if (opt.isEmpty()) {
            // Token não está mais no banco — tratamos como sucesso (já foi usado).
            return new ConfirmacaoEmailResult(ConfirmacaoStatus.SUCESSO, null);
        }

        ConfirmaToken token = opt.get();
        Usuario usuario = token.getUsuario() != null
            ? usuarioRepository.findByEmailIgnoreCase(token.getUsuario().getEmail())
            : null;

        // Usuário já validado antes → sempre sucesso (cliques repetidos no link).
        if (usuario != null && Boolean.TRUE.equals(usuario.getAtivado())) {
            return new ConfirmacaoEmailResult(ConfirmacaoStatus.SUCESSO, null);
        }

        // Token expirou e usuário ainda não foi ativado.
        if (token.isExpirado()) {
            return new ConfirmacaoEmailResult(ConfirmacaoStatus.EXPIRADO, null);
        }

        // Fluxo de primeira ativação.
        if (usuario == null) {
            return new ConfirmacaoEmailResult(ConfirmacaoStatus.INVALIDO, "Usuário não encontrado.");
        }

        usuario.setAtivado(true);
        usuarioRepository.save(usuario);
        return new ConfirmacaoEmailResult(ConfirmacaoStatus.SUCESSO, null);
    }

    /**
     * Normaliza a URL base do backend para os links de e-mail.
     * Se o valor de configuração já tem protocolo, usa como está; caso contrário
     * prefixa com https://. Nunca inclui porta (use o host completo direto).
     */
    private String buildBaseUrl() {
        if (url == null || url.isBlank()) {
            return "";
        }
        String trimmed = url.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed.replaceAll("/+$", "");
        }
        return "https://" + trimmed.replaceAll("/+$", "");
    }

    public ResponseEntity<Object> localizarPorUrl(String userUrl, String emailAutenticado) {
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailAutenticado)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado"));

        Optional<Usuario> usuario = usuarioRepository.findByUserUrl(userUrl);

        if (usuario.isEmpty()) {
            return ResponseHandler.generateResponse(
                String.format("Usuário não encontrado para url %s", userUrl), HttpStatus.NOT_FOUND);
        }

        if (usuario.get().isDeleted()) {
            return ResponseHandler.generateResponse("Usuário não existe mais!", HttpStatus.NOT_FOUND);
        }

        if (usuario.get().getId().equals(usuarioLogado.getId())) {
            MeuUsuarioResponse meuUsuarioResponse = new MeuUsuarioResponse();
            meuUsuarioResponse.inToOut(usuario.get());
            return ResponseHandler.generateResponse("Usuário encontrado!", HttpStatus.OK, meuUsuarioResponse);
        } else {
            OutroUsuarioResponse outroUsuarioResponse = new OutroUsuarioResponse();
            outroUsuarioResponse.inToOut(usuario.get());
            return ResponseHandler.generateResponse("Usuário encontrado!", HttpStatus.OK, outroUsuarioResponse);
        }
    }

    public Usuario localizarPorEmail(String email)
    throws UsuarioNaoVerificadoException, UsuarioRemovidoException, RecursoNaoEncontradoException {
        Usuario usuario = usuarioRepository
            .findByEmail(email)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado para o email: " + email));

        if (!usuario.getAtivado()) throw new UsuarioNaoVerificadoException(email);
        if (usuario.isDeleted()) throw new UsuarioRemovidoException();

        return usuario;
    }

    public Usuario localizar(UUID uuid) throws Exception {
        Usuario usuario = usuarioRepository.findById(uuid)
            .orElseThrow(() -> new Exception("Usuário não encontrado para o id: " + uuid));

        if (!usuario.getAtivado()) {
            throw new Exception("Usuário não foi ativado, verifique seu email:" + uuid);
        }

        if (usuario.isDeleted()) {
            throw new Exception("Usuário não existe mais");
        }

        return usuario;
    }

    public ResponseEntity<Object> recuperarSenha(LoginRequest recupera, String confirmaToken) throws Exception {
        ConfirmaToken token =
            confirmaRepository.findByToken(confirmaToken).orElseThrow(() -> new Exception("Token não encontrado"));

        if (token.isExpirado()) {
            confirmaRepository.delete(token);
            return ResponseHandler.generateResponse("Token expirado. Solicite um novo.", HttpStatus.BAD_REQUEST);
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(token.getUsuario().getEmail());
        usuario.setSenha(encoder.encode(recupera.getSenha()));
        usuarioRepository.save(usuario);
        confirmaRepository.delete(token);
        return ResponseHandler.generateResponse("Senha alterada com sucesso!", HttpStatus.OK);
    }

    public ResponseEntity<Object> recuperarSenha(LoginRequest email) throws Exception {
        Usuario usuario = localizarPorEmail(email.getEmail());

        long now = System.currentTimeMillis();
        ConfirmaToken confirmationToken = ConfirmaToken.builder()
            .token(UUID.randomUUID().toString())
            .usuario(usuario)
            .dataCriacao(new Timestamp(now))
            .dataExpiracao(new Timestamp(now + TOKEN_EXPIRATION_MS))
            .build();

        confirmaRepository.save(confirmationToken);

        String subject = "Recuperação de Senha";
        String text = "Para redefinir sua senha, clique no link abaixo:\n";
        String link = buildBaseUrl() + "/redefinir-senha?token=" + confirmationToken.getToken();
        String corpoEmail = emailService.criarCorpoEmail(usuario.getNome(), text, link);
        emailService.sendEmail(usuario.getEmail(), subject, corpoEmail);
        return ResponseHandler.generateResponse("Verifique seu email para instruções de recuperação de senha.", HttpStatus.OK);
    }

    public ResponseEntity<Object> salvarImagemPerfil(MultipartFile file, String emailAutenticado) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new Exception("Usuario não existe"));
            String urlImagem = documentoService.upload(file);
            usuario.setFotoPerfil(urlImagem);
            usuarioRepository.save(usuario);
            return ResponseHandler.generateResponse("Foto adicionada com sucesso", HttpStatus.OK, urlImagem);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao mandar a imagem.",
                HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> removerImagemPerfil(String emailAutenticado) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new Exception("Usuario não existe"));
            usuario.setFotoPerfil(null);
            usuarioRepository.save(usuario);
            return ResponseHandler.generateResponse("Foto removida com sucesso", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao remover a imagem.",
                HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> anexarCurriculo(MultipartFile file, String nome, String emailAutenticado) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new Exception("Usuario não existe"));

            Curriculo novoCurriculo = Curriculo.builder()
                .curriculo(documentoService.upload(file))
                .nomeCurriculo(nome)
                .usuario(usuario)
                .build();

            curriculoRepository.save(novoCurriculo);
            return ResponseHandler.generateResponse("Currículo adicionado com sucesso", HttpStatus.OK, novoCurriculo.getCurriculo());
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao enviar o currículo.",
                HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> removerCurriculo(Integer id, String emailAutenticado) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new Exception("Usuario não existe"));

            Curriculo curriculoParaRemover = usuario.getCurriculo()
                .stream()
                .filter(curriculo -> curriculo.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new Exception("Currículo não encontrado"));

            usuario.getCurriculo().remove(curriculoParaRemover);
            usuarioRepository.save(usuario);
            return ResponseHandler.generateResponse("Currículo removido com sucesso", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao remover o currículo.",
                HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> inativarUsuario(UUID idUsuario) {
        try {
            Usuario usuarioDeletado = localizar(idUsuario);
            usuarioDeletado.setDeletado(true);
            usuarioDeletado.setDeletadoEm(LocalDateTime.now());
            usuarioRepository.save(usuarioDeletado);
            return ResponseHandler.generateResponse("Usuário inativado com sucesso!", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> reativarUsuario(UUID idUsuario) {
        try {
            Usuario usuarioDeletado = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new Exception("Usuário não encontrado"));
            usuarioDeletado.setDeletado(false);
            usuarioDeletado.setDeletadoEm(null);
            usuarioRepository.save(usuarioDeletado);
            return ResponseHandler.generateResponse("Usuário ativado com sucesso!", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> listarCurriculo(String emailAutenticado) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new Exception("Usuario não existe"));
            return ResponseHandler.generateResponse("Todos os currículos do usuário", HttpStatus.OK, usuario.getCurriculo());
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao listar os currículos.",
                HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listar(String userUrl, Pageable pageable) {
        try {
            if (Strings.isEmpty(userUrl)) {
                Page<Usuario> usuarios = usuarioRepository.findAll(pageable);
                List<OutroUsuarioResponse> usuarioResponses = usuarios.getContent().stream().map(u -> {
                    OutroUsuarioResponse resp = new OutroUsuarioResponse();
                    resp.inToOut(u);
                    return resp;
                }).collect(Collectors.toList());
                return ResponseHandler.generateResponse("Filtro de usuário feito!", HttpStatus.OK, usuarioResponses);
            } else {
                Page<Usuario> usuarios = usuarioRepository.findAllByNomeContainingIgnoreCase(userUrl, pageable);
                List<OutroUsuarioResponse> usuarioResponses = usuarios.getContent().stream().map(u -> {
                    OutroUsuarioResponse resp = new OutroUsuarioResponse();
                    resp.inToOut(u);
                    return resp;
                }).collect(Collectors.toList());
                return ResponseHandler.generateResponse("Filtro de usuário feito!", HttpStatus.OK, usuarioResponses);
            }
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao filtrar usuários.",
                HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listarCandidaturas(String emailAutenticado) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new Exception("Usuario não existe"));
            List<Vaga> vagas = vagaRepository.findAllById(
                candidaturaRepository.findAllCandidaturaByUsuarioId(usuario.getId())
                    .stream()
                    .map(candidatura -> candidatura.getVaga().getId())
                    .collect(Collectors.toList()));
            return ResponseHandler.generateResponse("Todos as vagas candidatadas do usuario", HttpStatus.OK, vagas);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao listar as candidaturas.",
                HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
