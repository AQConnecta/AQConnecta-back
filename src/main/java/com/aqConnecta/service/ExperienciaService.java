package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.ExperienciaRequest;
import com.aqConnecta.DTOs.request.UsuarioRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.model.Experiencia;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.repository.ExperienciaRepository;
import com.aqConnecta.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class ExperienciaService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ExperienciaRepository experienciaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    public ResponseEntity<Object> cadastrarExperiencia(ExperienciaRequest registro) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        if (!registro.validarDadosObrigatorios()) {
            return ResponseHandler.generateResponse("Error: Campos obrigatorios não informados", HttpStatus.BAD_REQUEST);
        }
        try {
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            Experiencia experiencia = new Experiencia().builder()
                    .id(UUID.randomUUID())
                    .usuario(usuario)
                    .titulo(registro.getTitulo())
                    .instituicao(registro.getInstituicao())
                    .descricao(registro.getDescricao())
                    .dataInicio(registro.getDataInicio())
                    .build();
            if (registro.getDataFim() != null) {
                experiencia.setDataFim(registro.getDataFim());
            }
            experiencia.setAtualExperiencia(registro.isAtualExperiencia());
            experienciaRepository.save(experiencia);
            return ResponseHandler.generateResponse("Experiencia cadastrada com súcesso!", HttpStatus.CREATED, experiencia);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> cadastrarDescricaoUsuario(UsuarioRequest usuarioRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            usuario.setDescricao(usuarioRequest.getDescricao());
            usuarioRepository.save(usuario);
            return ResponseHandler.generateResponse("Descrição cadastrada com súcesso!", HttpStatus.CREATED, usuario);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> listarExperienciasPorUsuario(UUID idUsuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
            }
        try {
            Usuario usuario = usuarioService.localizar(idUsuario);
            Set<Experiencia> experiencias = experienciaRepository.findByUsuario(usuario);
            if (!experiencias.isEmpty()) {
                return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, experiencias);
            }
            return ResponseHandler.generateResponse("Nenhum experiencia encontrada para este usuário.", HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as experiencias do usuário.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> localizarExperiencia(UUID idExperiencia) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
            }
        try {
            Optional<Experiencia> experiencia = experienciaRepository.findById(idExperiencia);
            if (experiencia.isPresent()) {
                return ResponseHandler.generateResponse("Localizado com sucesso", HttpStatus.OK, experiencia);
            }
            return ResponseHandler.generateResponse("Nenhum experiencia encontrada para este ID.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar localizar a experiencia.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    public ResponseEntity<Object> alterarExperiencia(UUID idExperiencia, ExperienciaRequest registro) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // TODO remover essa bosta de contains dps do riume arrumar o security
            if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
            }
            if (!registro.validarDadosObrigatorios() && idExperiencia != null && !idExperiencia.toString().isEmpty()) {
                return ResponseHandler.generateResponse("Error: Campos obrigatorios não informados", HttpStatus.BAD_REQUEST);
            }
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            Optional<Experiencia> experiencia = experienciaRepository.findById(idExperiencia);
            if (experiencia.isPresent()) {
                if (!experiencia.get().getUsuario().getId().equals(usuario.getId())) {
                    return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.", HttpStatus.UNAUTHORIZED);
                }
                Experiencia experienciaAlterada = new Experiencia().builder()
                        .id(idExperiencia)
                        .usuario(experiencia.get().getUsuario())
                        .titulo(registro.getTitulo())
                        .instituicao(registro.getInstituicao())
                        .descricao(registro.getDescricao())
                        .dataInicio(registro.getDataInicio())
                        .build();
                if (registro.getDataFim() != null) {
                    experienciaAlterada.setDataFim(registro.getDataFim());
                }
                // TODO verificar porque da dando ruim se nao passar ele indo pra false de qualuqer jeito tmj
                if (experiencia.get().isAtualExperiencia() != registro.isAtualExperiencia()) {
                    experienciaAlterada.setAtualExperiencia(registro.isAtualExperiencia());
                }
                experienciaRepository.save(experienciaAlterada);
                return ResponseHandler.generateResponse("Experiencia atualizada com súcesso!", HttpStatus.CREATED, experiencia);
            }
            return ResponseHandler.generateResponse("Erro ao encontrar a experiencia!", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<Object> deletarExperiencia(UUID idExperiencia) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // TODO remover essa bosta de contains dps do riume arrumar o security
            if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
            }
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            Optional<Experiencia> experiencia = experienciaRepository.findById(idExperiencia);
            if (experiencia.isPresent()) {
                if (!experiencia.get().getUsuario().getId().equals(usuario.getId())) {
                    return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
                }
                experienciaRepository.deleteById(idExperiencia);
            } else {
                return ResponseHandler.generateResponse("Não é possível excluir uma experiencia não existente.", HttpStatus.NOT_FOUND);
            }
            return ResponseHandler.generateResponse("Deletado com sucesso", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar excluir a experiencia.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private boolean isUserAnonymous(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName());
    }

}
