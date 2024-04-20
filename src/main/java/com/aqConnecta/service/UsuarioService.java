package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.LoginRequest;
import com.aqConnecta.DTOs.request.RegistroRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.model.ConfirmaToken;
import com.aqConnecta.model.Permissao;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.repository.ConfirmaRepository;
import com.aqConnecta.repository.PermissaoRepository;
import com.aqConnecta.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    @Value("${url}")
    private String url;

    @Value("${port}")
    private String port;

    @Autowired
    private PasswordEncoder encoder;

    public ResponseEntity<?> saveUsuario(RegistroRequest registro) throws RuntimeException {

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

    public ResponseEntity<?> confirmaEmail(String confirmaToken) throws Exception {
        ConfirmaToken token = confirmaRepository.findByToken(confirmaToken).orElseThrow(() -> new Exception("Token não encontrado"));

        if (token != null) {
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(token.getUsuario().getEmail());
            usuario.setAtivado(true);
            usuarioRepository.save(usuario);
            confirmaRepository.delete(token);
            return ResponseHandler.generateResponse("Email verificado com sucesso!", HttpStatus.OK, usuario.getUsuarioSemSenha());
        }
        return ResponseHandler.generateResponse("Error: Não foi possivel verificar o email", HttpStatus.BAD_REQUEST, null);
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


    public ResponseEntity<?> recuperarSenha(LoginRequest recupera, String confirmaToken) throws Exception {
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

    public ResponseEntity<?> recuperarSenha(LoginRequest email) throws Exception {
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

}
