package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.FormacaoAcademicaRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.model.FormacaoAcademica;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.repository.FormacaoAcademicaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FormacaoAcademicaService {

    @Autowired
    private DocumentoService documentoService;

    public ResponseEntity<Object> uploadDiploma(MultipartFile file) {
        try {
            String url = documentoService.upload(file);
            return ResponseHandler.generateResponse("Diploma anexado com sucesso!", HttpStatus.OK, url);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao anexar o diploma.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private FormacaoAcademicaRepository formacaoAcademicaRepository;

    public ResponseEntity<Object> cadastrarFormacaoAcademica(FormacaoAcademicaRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            FormacaoAcademica formacaoAcademica = FormacaoAcademica.builder()
                    .usuario(usuario)
                    .universidade(registro.getUniversidade())
                    .descricao(registro.getDescricao())
                    .diploma(registro.getDiploma())
                    .dataInicio(registro.getDataInicio())
                    .dataFim(registro.getDataFim())
                    .atualFormacao(registro.isAtualFormacao())
                    .build();
            formacaoAcademicaRepository.save(formacaoAcademica);
            return ResponseHandler.generateResponse("Formação academica cadastrada com sucesso!", HttpStatus.CREATED, formacaoAcademica);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> listarFormacaoAcademicaPorUsuario(UUID idUsuario) {
        try {
            Usuario usuario = usuarioService.localizar(idUsuario);
            Set<FormacaoAcademica> formacoesAcademicas = formacaoAcademicaRepository.findByUsuario(usuario);
            if (!formacoesAcademicas.isEmpty()) {
                return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, formacoesAcademicas);
            }
            return ResponseHandler.generateResponse("Nenhuma formação academica encontrada para este usuário.", HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as formações academicas do usuário.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> localizarFormacaoAcademica(UUID idFormacaoAcademia) {
        try {
            Optional<FormacaoAcademica> formacaoAcademica = formacaoAcademicaRepository.findById(idFormacaoAcademia);
            if (formacaoAcademica.isPresent()) {
                return ResponseHandler.generateResponse("Localizado com sucesso", HttpStatus.OK, formacaoAcademica);
            }
            return ResponseHandler.generateResponse("Nenhuma formação academica encontrada para este ID.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar localizar a formação academica.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> alterarFormacaoAcademica(UUID idFormacaoAcademia, FormacaoAcademicaRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<FormacaoAcademica> formacaoAcademica = formacaoAcademicaRepository.findById(idFormacaoAcademia);
            if (formacaoAcademica.isPresent()) {
                if (!formacaoAcademica.get().getUsuario().getId().equals(usuario.getId())) {
                    return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
                }
                FormacaoAcademica formacaoAcademicaAlterada = FormacaoAcademica.builder()
                        .id(idFormacaoAcademia)
                        .usuario(usuario)
                        .universidade(registro.getUniversidade())
                        .descricao(registro.getDescricao())
                        .diploma(registro.getDiploma())
                        .dataInicio(registro.getDataInicio())
                        .dataFim(registro.getDataFim())
                        .atualFormacao(registro.isAtualFormacao())
                        .build();
                formacaoAcademicaRepository.save(formacaoAcademicaAlterada);
                return ResponseHandler.generateResponse("Formação academica atualizada com sucesso!", HttpStatus.OK, formacaoAcademicaAlterada);
            }
            return ResponseHandler.generateResponse("Erro ao encontrar a formação academica!", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> deletarFormacaoAcademica(UUID idFormacaoAcademica, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<FormacaoAcademica> formacaoAcademica = formacaoAcademicaRepository.findById(idFormacaoAcademica);
            if (formacaoAcademica.isPresent()) {
                if (!formacaoAcademica.get().getUsuario().getId().equals(usuario.getId())) {
                    return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
                }
                formacaoAcademicaRepository.deleteById(idFormacaoAcademica);
                return ResponseHandler.generateResponse("Deletado com sucesso", HttpStatus.OK);
            }
            return ResponseHandler.generateResponse("Não é possível excluir uma formação academica não existente.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar excluir a formação academica.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
