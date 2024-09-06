package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.LoginRequest;
import com.aqConnecta.DTOs.request.RegistroRequest;
import com.aqConnecta.DTOs.response.MeuUsuarioResponse;
import com.aqConnecta.DTOs.response.OutroUsuarioResponse;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.model.*;
import com.aqConnecta.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UsuarioService {

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

    @Value("${url}")
    private String url;

    @Value("${port}")
    private String port;

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private VagaRepository vagaRepository;
    @Autowired
    private CandidaturaRepository candidaturaRepository;

    public ResponseEntity<Object> saveUsuario(RegistroRequest registro) throws RuntimeException {

        if (usuarioRepository.existsByEmail(registro.getEmail())) {
            return ResponseHandler.generateResponse("Erro: Email já está em uso!", HttpStatus.CONFLICT, null);
        }

        Set<Permissao> permissoes = new HashSet<>();
        permissoes.add(permissaoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Erro interno, não foi possivel criar conta com permissão de cliente")));

        Usuario usuario = new Usuario().builder()
                .id(UUID.randomUUID())
                .nome(registro.getNome())
                .email(registro.getEmail())
                .senha(encoder.encode(registro.getSenha()))
                .permissao(permissoes)
                .userUrl(generateUserUrl(registro.getNome()))
                .build();

        usuarioRepository.save(usuario);

        ConfirmaToken confirmationToken = new ConfirmaToken().builder()
                .token(UUID.randomUUID().toString())
                .usuario(usuario)
                .dataCriacao(new Timestamp(System.currentTimeMillis()))
                .build();

        confirmaRepository.save(confirmationToken);

        String subject = "Complete a inscrição!";
        String text = "Para confirmar a conta, por favor clique aqui :";
        String link = "http://" + url + ":" + port + "/auth/confirma-conta?token=" + confirmationToken.getToken();
        String corpoEmail = emailService.criarCorpoEmail(usuario.getNome(), text, link);
        emailService.sendEmail(usuario.getEmail(), subject, corpoEmail);

        return ResponseHandler.generateResponse("Verifique seu e-mail", HttpStatus.OK, usuario.getUsuarioSemSenha());
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
        ConfirmaToken token = confirmaRepository.findByToken(confirmaToken).orElseThrow(() -> new Exception("Token não encontrado"));

        if (token != null) {
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(token.getUsuario().getEmail());
            usuario.setAtivado(true);
            usuarioRepository.save(usuario);
            confirmaRepository.delete(token);
            return ResponseHandler.generateResponse("Email verificado com sucesso!", HttpStatus.OK, null);
        }
        return ResponseHandler.generateResponse("Error: Não foi possivel verificar o email", HttpStatus.BAD_REQUEST, null);
    }

    public ResponseEntity<Object> localizarPorUrl(String userUrl) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }

        assert authentication != null;
        String username = (String) authentication.getPrincipal();
        Usuario usuarioLogado = usuarioRepository.findByEmail(username).get();

        Optional<Usuario> usuario = usuarioRepository.findByUserUrl(userUrl);

        if (usuario.isEmpty()) {
            return ResponseHandler.generateResponse(String.format("Usuário não encontrado para url %s", userUrl), HttpStatus.NOT_FOUND, null);
        }

        if (usuario.get().getDeletado()) {
            return ResponseHandler.generateResponse("Usuário não existe mais!", HttpStatus.NOT_FOUND, null);
        }

        if (usuario.get().getId() == usuarioLogado.getId()) {
            MeuUsuarioResponse meuUsuarioResponse = new MeuUsuarioResponse();
            meuUsuarioResponse.inToOut(usuario.get());
            return ResponseHandler.generateResponse("Usuário encontrado!", HttpStatus.OK, meuUsuarioResponse);
        } else {
            OutroUsuarioResponse outroUsuarioResponse = new OutroUsuarioResponse();
            outroUsuarioResponse.inToOut(usuario.get());
            return ResponseHandler.generateResponse("Usuário encontrado!", HttpStatus.OK, outroUsuarioResponse);
        }
    }

    public Usuario localizarPorEmail(String email) throws Exception {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Usuário não encontrado para o email: " + email));

        if (!usuario.getAtivado()) {
            throw new Exception("Usuário não foi ativado, verifique seu email:" + email);
        }

        if (usuario.getDeletado()) {
            throw new Exception("Usuário não existe mais");
        }

        return usuario;
    }

    public Usuario localizar(UUID uuid) throws Exception {
        Usuario usuario = usuarioRepository.findById(uuid)
                .orElseThrow(() -> new Exception("Usuário não encontrado para o id: " + uuid.toString()));

        if (!usuario.getAtivado()) {
            throw new Exception("Usuário não foi ativado, verifique seu email:" + uuid.toString());
        }

        if (usuario.getDeletado()) {
            throw new Exception("Usuário não existe mais");
        }

        return usuario;
    }


    public ResponseEntity<Object> recuperarSenha(LoginRequest recupera, String confirmaToken) throws Exception {
        ConfirmaToken token = confirmaRepository.findByToken(confirmaToken).orElseThrow(() -> new Exception("Token não encontrado"));

        if (token != null) {
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(token.getUsuario().getEmail());
            usuario.setSenha(encoder.encode(recupera.getSenha()));
            usuarioRepository.save(usuario);
            confirmaRepository.delete(token);
            return ResponseHandler.generateResponse("Senha alterara com sucesso!.", HttpStatus.OK, null);
        }
        return ResponseHandler.generateResponse("Error: Não foi possivel alterar a senha", HttpStatus.BAD_REQUEST, null);
    }

    public ResponseEntity<Object> recuperarSenha(LoginRequest email) throws Exception {
        Usuario usuario = localizarPorEmail(email.getEmail());

        ConfirmaToken confirmationToken = new ConfirmaToken().builder()
                .token(UUID.randomUUID().toString())
                .usuario(usuario)
                .dataCriacao(new Timestamp(System.currentTimeMillis()))
                .build();

        confirmaRepository.save(confirmationToken);

        String subject = "Recuperação de Senha";
        String text = "Para redefinir sua senha, clique no link abaixo:\n";
        String link = "http://" + url + ":" + port + "/auth/recuperando?token=" + confirmationToken.getToken();
        String corpoEmail = emailService.criarCorpoEmail(usuario.getNome(), text, link);
        emailService.sendEmail(usuario.getEmail(), subject, corpoEmail);
        return ResponseHandler.generateResponse("Verifique seu email para instruções de recuperação de senha.", HttpStatus.OK, null);
    }

    public ResponseEntity<Object> salvarImagemPerfil(MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }

        try {
            assert authentication != null;
            String username = (String) authentication.getPrincipal();
            Usuario usuario = usuarioRepository.findByEmail(username).orElseThrow(() -> new Exception("Usuario não existe"));
            usuario.setFotoPerfil(documentoService.upload(file));
            usuarioRepository.save(usuario);

            return ResponseHandler.generateResponse("Foto adicionada com sucesso", HttpStatus.OK, documentoService.upload(file));
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao mandar a imagem.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> removerImagemPerfil() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }

        try {
            assert authentication != null;
            String username = (String) authentication.getPrincipal();
            Usuario usuario = usuarioRepository.findByEmail(username).orElseThrow(() -> new Exception("Usuario não existe"));
            usuario.setFotoPerfil(null);
            usuarioRepository.save(usuario);

            return ResponseHandler.generateResponse("Foto removida com sucesso", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao remover a imagem.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> anexarCurriculo(MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se o usuário está autenticado e não é anônimo
        if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }

        try {
            assert authentication != null;
            String username = (String) authentication.getPrincipal();
            Usuario usuario = usuarioRepository.findByEmail(username).orElseThrow(() -> new Exception("Usuario não existe"));

            if (usuario.getCurriculo() == null) {
                usuario.setCurriculo(new HashMap<>());
            }

            int proximaSequencia = usuario.getCurriculo().keySet().stream()
                    .max(Integer::compareTo)
                    .orElse(0) + 1;

            usuario.getCurriculo().put(proximaSequencia, documentoService.upload(file));
            usuarioRepository.save(usuario);

            return ResponseHandler.generateResponse("Currículo adicionado com sucesso", HttpStatus.OK, documentoService.upload(file));
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao enviar o currículo.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> removerCurriculo(Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se o usuário está autenticado e não é anônimo
        if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }

        try {
            assert authentication != null;
            String username = (String) authentication.getPrincipal();
            Usuario usuario = usuarioRepository.findByEmail(username).orElseThrow(() -> new Exception("Usuario não existe"));

            usuario.getCurriculo().remove(id);
            usuarioRepository.save(usuario);

            return ResponseHandler.generateResponse("Currículo removido com sucesso", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao remover o currículo.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private boolean isUserAnonymous(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName());
    }

    public ResponseEntity<Object> inativarUsuario(UUID idUsuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Usuario usuario = localizarPorEmail(authentication.getName());
            if (usuario.verificarUsuarioNaoEAdministrador()) {
                return ResponseHandler.generateResponse("Você não tem permissão para inativar um usuário.", HttpStatus.FORBIDDEN);
            }
            Usuario usuarioDeletado = localizar(idUsuario);
            usuarioDeletado.setDeletado(true);
            usuarioRepository.save(usuarioDeletado);
            return ResponseHandler.generateResponse("Usuário inativado com súcesso!", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> reativarUsuario(UUID idUsuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Usuario usuario = localizarPorEmail(authentication.getName());
            if (usuario.verificarUsuarioNaoEAdministrador()) {
                return ResponseHandler.generateResponse("Você não tem permissão para reativar um usuário.", HttpStatus.FORBIDDEN);
            }
            Usuario usuarioDeletado = usuarioRepository.findById(idUsuario).get();
            usuarioDeletado.setDeletado(false);
            usuarioRepository.save(usuarioDeletado);
            return ResponseHandler.generateResponse("Usuário ativado com súcesso!", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> listarCurriculo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se o usuário está autenticado e não é anônimo
        if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }

        try {
            assert authentication != null;
            String username = (String) authentication.getPrincipal();
            Usuario usuario = usuarioRepository.findByEmail(username).orElseThrow(() -> new Exception("Usuario não existe"));

            return ResponseHandler.generateResponse("Todos os curriculos do usuario", HttpStatus.OK, usuario.getCurriculo());
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao remover o currículo.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listar(String userUrl) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se o usuário está autenticado e não é anônimo
        if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }

        try {
            assert authentication != null;
            List<Usuario> usuarios;
            usuarios = Strings.isEmpty(userUrl) ? usuarioRepository.findAll() : usuarioRepository.findAllByNomeContainingIgnoreCase(userUrl);
            List<OutroUsuarioResponse> usuarioResponses = new ArrayList<>();
            usuarios.forEach(usuario -> {
                OutroUsuarioResponse usuarioResponse = new OutroUsuarioResponse();
                usuarioResponse.inToOut(usuario);
                usuarioResponses.add(usuarioResponse);
            });
            return ResponseHandler.generateResponse("Filtro de usuário feito!", HttpStatus.OK, usuarioResponses);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao filtrar usuários.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    public ResponseEntity<Object> listarCandidaturas() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se o usuário está autenticado e não é anônimo
        if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }

        try {
            assert authentication != null;
            String username = (String) authentication.getPrincipal();
            Usuario usuario = usuarioRepository.findByEmail(username).orElseThrow(() -> new Exception("Usuario não existe"));
            List<Vaga> vagas = vagaRepository.findAllById(candidaturaRepository.findAllCandidaturaByUsuarioId(usuario.getId())
                    .stream()
                    .map(candidatura -> candidatura.getVaga().getId())
                    .collect(Collectors.toList()));
            return ResponseHandler.generateResponse("Todos as vagas candidatadas do usuario", HttpStatus.OK, vagas);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao listar as candidaturas.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
