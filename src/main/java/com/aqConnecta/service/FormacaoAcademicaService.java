package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.FormacaoAcademicaRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.exception.base.ErroInternoDoServidorException;
import com.aqConnecta.model.FormacaoAcademica;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.repository.FormacaoAcademicaRepository;
import com.aqConnecta.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FormacaoAcademicaService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private FormacaoAcademicaRepository formacaoAcademicaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public ResponseEntity<Object> cadastrarFormacaoAcademica(FormacaoAcademicaRequest registro) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            FormacaoAcademica formacaoAcademica = FormacaoAcademica.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .universidade(registro.getUniversidade())
                .descricao(registro.getDescricao())
                .diploma(registro.getDiploma())
                .dataInicio(registro.getDataInicio())
                .dataFim(registro.getDataFim())
                .build();
            if (registro.getDataFim() != null) {
                formacaoAcademica.setDataFim(registro.getDataFim());
            }
            formacaoAcademica.setAtualFormacao(registro.isAtualFormacao());
            // pode dar erro se não existir uma universidade com o ID fornecido, e é um erro
            // tratável...
            formacaoAcademicaRepository.save(formacaoAcademica);
            return ResponseHandler.generateResponse("Formação academica cadastrada com súcesso!",
                HttpStatus.CREATED,
                formacaoAcademica);
        }
        catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> listarFormacaoAcademicaPorUsuario(UUID idUsuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Usuario usuario = usuarioService.localizar(idUsuario);
            Set<FormacaoAcademica> formacoesAcademicas = formacaoAcademicaRepository.findByUsuario(usuario);
            if (!formacoesAcademicas.isEmpty()) {
                return ResponseHandler.generateResponse("Listagem feita com sucesso!",
                    HttpStatus.OK,
                    formacoesAcademicas);
            }
            return ResponseHandler.generateResponse("Nenhum formação academica encontrada para este usuário.",
                HttpStatus.NO_CONTENT);
        }
        catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as formações academicas do usuário.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage());
        }
    }

    public ResponseEntity<Object> localizarFormacaoAcademica(UUID idFormacaoAcademia) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Optional<FormacaoAcademica> formacaoAcademica = formacaoAcademicaRepository.findById(idFormacaoAcademia);
            if (formacaoAcademica.isPresent()) {
                return ResponseHandler.generateResponse("Localizado com sucesso", HttpStatus.OK, formacaoAcademica);
            }
            return ResponseHandler.generateResponse("Nenhum formação academica encontrada para este ID.",
                HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar localizar a formação academica.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage());
        }
    }


    public ResponseEntity<Object> alterarFormacaoAcademica(UUID idFormacaoAcademia, FormacaoAcademicaRequest registro) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // TODO remover essa bosta de contains dps do riume arrumar o security
            if (authentication != null && authentication.isAuthenticated() && authentication.getName()
                .toLowerCase()
                .contains("anonymous")) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.",
                    HttpStatus.UNAUTHORIZED);
            }

            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            Optional<FormacaoAcademica> formacaoAcademica = formacaoAcademicaRepository.findById(idFormacaoAcademia);
            if (formacaoAcademica.isPresent()) {
                if (!formacaoAcademica.get().getUsuario().getId().equals(usuario.getId())) {
                    return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.",
                        HttpStatus.UNAUTHORIZED);
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
                if (registro.getDataFim() != null) {
                    formacaoAcademicaAlterada.setDataFim(registro.getDataFim());
                }
                // TODO verificar porque da dando ruim se nao passar ele indo pra false de qualuqer jeito tmj
                if (formacaoAcademica.get().getAtualFormacao() != registro.isAtualFormacao()) {
                    formacaoAcademicaAlterada.setAtualFormacao(registro.isAtualFormacao());
                }
                formacaoAcademicaRepository.save(formacaoAcademicaAlterada);
                return ResponseHandler.generateResponse("Formação academica atualizada com súcesso!",
                    HttpStatus.CREATED,
                    formacaoAcademicaAlterada);
            }
            return ResponseHandler.generateResponse("Erro ao encontrar a formação academica!", HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public void deletarFormacaoAcademica(UUID idFormacaoAcademica, Usuario usuario) {
        try {
            // ignora silenciosamente casos onde:
            // * formação não existe (idempotência); ou
            // * formação não pertence a esse usuário (ele não precisa ser informado que não lhe pertence,
            //   pois provavelmente está tentando acessar um recurso de outro usuário indevidamente).
            formacaoAcademicaRepository.deleteFormacaoAcademicaByIdAndUsuarioId(idFormacaoAcademica, usuario.getId());
        }
        catch (Exception exception) {
            final var errorMessage = "Não foi possível excluir a formação acadêmica devido a problemas no servidor.";
            throw new ErroInternoDoServidorException(errorMessage, exception);
        }
    }

    private boolean isUserAnonymous(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated()
               || "anonymousUser".equals(authentication.getName());
    }

}
