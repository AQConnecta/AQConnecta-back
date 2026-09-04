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

    public ResponseEntity<Object> cadastrarExperiencia(ExperienciaRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Experiencia experiencia = Experiencia.builder()
                    .usuario(usuario)
                    .titulo(registro.getTitulo())
                    .instituicao(registro.getInstituicao())
                    .descricao(registro.getDescricao())
                    .dataInicio(registro.getDataInicio())
                    .atualExperiencia(registro.isAtualExperiencia())
                    .build();
            if (registro.getDataFim() != null) {
                experiencia.setDataFim(registro.getDataFim());
            }
            experienciaRepository.save(experiencia);
            return ResponseHandler.generateResponse("Experiencia cadastrada com sucesso!", HttpStatus.CREATED, experiencia);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> cadastrarDescricaoUsuario(UsuarioRequest usuarioRequest, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            usuario.setDescricao(usuarioRequest.getDescricao());
            usuarioRepository.save(usuario);
            return ResponseHandler.generateResponse("Descrição cadastrada com sucesso!", HttpStatus.CREATED, usuario);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> listarExperienciasPorUsuario(UUID idUsuario) {
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

    public ResponseEntity<Object> alterarExperiencia(UUID idExperiencia, ExperienciaRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Experiencia> experiencia = experienciaRepository.findById(idExperiencia);
            if (experiencia.isPresent()) {
                if (!experiencia.get().getUsuario().getId().equals(usuario.getId())) {
                    return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
                }
                Experiencia experienciaAlterada = Experiencia.builder()
                        .id(idExperiencia)
                        .usuario(experiencia.get().getUsuario())
                        .titulo(registro.getTitulo())
                        .instituicao(registro.getInstituicao())
                        .descricao(registro.getDescricao())
                        .dataInicio(registro.getDataInicio())
                        .atualExperiencia(registro.isAtualExperiencia())
                        .build();
                if (registro.getDataFim() != null) {
                    experienciaAlterada.setDataFim(registro.getDataFim());
                }
                experienciaRepository.save(experienciaAlterada);
                return ResponseHandler.generateResponse("Experiencia atualizada com sucesso!", HttpStatus.OK, experienciaAlterada);
            }
            return ResponseHandler.generateResponse("Erro ao encontrar a experiencia!", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> deletarExperiencia(UUID idExperiencia, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Experiencia> experiencia = experienciaRepository.findById(idExperiencia);
            if (experiencia.isPresent()) {
                if (!experiencia.get().getUsuario().getId().equals(usuario.getId())) {
                    return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
                }
                experienciaRepository.deleteById(idExperiencia);
                return ResponseHandler.generateResponse("Deletado com sucesso", HttpStatus.OK);
            }
            return ResponseHandler.generateResponse("Não é possível excluir uma experiencia não existente.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar excluir a experiencia.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
