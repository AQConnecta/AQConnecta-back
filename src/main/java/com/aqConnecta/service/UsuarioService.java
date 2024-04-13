package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.RegistroRequest;
import com.aqConnecta.model.ConfirmaToken;
import com.aqConnecta.model.Permissao;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.repository.ConfirmaRepository;
import com.aqConnecta.repository.PermissaoRepository;
import com.aqConnecta.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
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

    public ResponseEntity<?> saveUsuario(RegistroRequest registro) throws RuntimeException{

        if (usuarioRepository.existsByEmail(registro.getEmail())) {
            return ResponseEntity.badRequest().body("Error: já está em uso!");
        }

        Set<Permissao> permissoes= new HashSet<>();
        permissoes.add(permissaoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Erro interno, não foi possivel criar conta com permissão de cliente")));

        Usuario usuario = new Usuario().builder()
                    .id(UUID.randomUUID())
                    .nome(registro.getNome())
                    .email(registro.getEmail())
                    .senha(encoder.encode(registro.getSenha()))
                    .permissao(permissoes )
                    .build();

        usuarioRepository.save(usuario);

        ConfirmaToken confirmationToken = new ConfirmaToken().builder()
                                            .token(UUID.randomUUID().toString())
                                            .usuario(usuario)
                                            .dataCriacao(new Timestamp(System.currentTimeMillis()))
                                            .build();

        confirmaRepository.save(confirmationToken);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(usuario.getEmail());
        mailMessage.setSubject("Complete a inscrição!");
        mailMessage.setText("para confirmar a conta, por favor clique aqui : "
                +"http://" + url + ":" + port + "/auth/confirma-conta?token="+confirmationToken.getToken());
        emailService.sendEmail(mailMessage);

        System.out.println("Confirmation Token: " + confirmationToken.getToken());

        return ResponseEntity.ok("Verify email by the link sent on your email address");
    }

    public ResponseEntity<?> confirmaEmail(String confirmaToken) throws Exception {
        ConfirmaToken token = confirmaRepository.findByToken(confirmaToken).orElseThrow(() -> new Exception("Token não encontrado"));

        if(token != null)
        {
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(token.getUsuario().getEmail());
            usuario.setAtivado(true);
            usuarioRepository.save(usuario);
            return ResponseEntity.ok("Email varificado com sucesso!");
        }
        return ResponseEntity.badRequest().body("Error: Não foi possível verificar o email");
    }

    public Usuario localizarPorEmail(String email) throws Exception {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Usuário não encontrado para o email: " + email));
    }
}
