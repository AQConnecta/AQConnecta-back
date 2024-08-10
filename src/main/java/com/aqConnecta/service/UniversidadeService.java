package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.UniversidadeRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.model.Universidade;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.repository.UniversidadeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UniversidadeService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UniversidadeRepository universidadeRepository;

    public ResponseEntity<Object> cadastrarUniversidade(UniversidadeRequest registro) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            if (usuario.verificarUsuarioNaoEAdministrador()) {
                return ResponseHandler.generateResponse("Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
            }
            Universidade universidadeAlterada = new Universidade().builder()
                    .id(UUID.randomUUID())
                    .codigoIes(registro.getCodigoIes())
                    .nomeInstituicao(registro.getNomeInstituicao())
                    .sigla(registro.getSigla())
                    .categoriaIes(registro.getCategoriaIes())
                    .organizacaoAcademica(registro.getOrganizacaoAcademica())
                    .codigoMunicipioIbge(registro.getCodigoMunicipioIbge())
                    .municipio(registro.getMunicipio())
                    .uf(registro.getUf())
                    .situacaoIes(registro.getSituacaoIes())
                    .build();
            universidadeRepository.save(universidadeAlterada);
            return ResponseHandler.generateResponse("Universidade cadastrada com súcesso!", HttpStatus.CREATED, universidadeAlterada);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> listarUniversidades() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            List<Universidade> universidades = universidadeRepository.findAll();
            if (!universidades.isEmpty()) {
                return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, universidades);
            }
            return ResponseHandler.generateResponse("Nenhum universidade encontrada para este usuário.", HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as universidades.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> localizarUniversidade(UUID idUniversidade) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (isUserAnonymous(authentication)) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Optional<Universidade> universidade = universidadeRepository.findById(idUniversidade);
            if (universidade.isPresent()) {
                return ResponseHandler.generateResponse("Localizado com sucesso", HttpStatus.OK, universidade);
            }
            return ResponseHandler.generateResponse("Nenhum universidade encontrada para este ID.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar localizar a universidade.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    public ResponseEntity<Object> alterarUniversidade(UUID idUniversidade, UniversidadeRequest registro) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // TODO remover essa bosta de contains dps do riume arrumar o security
            if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
            }
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            Optional<Universidade> universidade = universidadeRepository.findById(idUniversidade);
            if (usuario.verificarUsuarioNaoEAdministrador()) {
                return ResponseHandler.generateResponse("Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
            }
            if (universidade.isPresent()) {
                Universidade universidadeAlterada = new Universidade().builder()
                        .id(idUniversidade)
                        .codigoIes(registro.getCodigoIes())
                        .nomeInstituicao(registro.getNomeInstituicao())
                        .sigla(registro.getSigla())
                        .categoriaIes(registro.getCategoriaIes())
                        .organizacaoAcademica(registro.getOrganizacaoAcademica())
                        .codigoMunicipioIbge(registro.getCodigoMunicipioIbge())
                        .municipio(registro.getMunicipio())
                        .uf(registro.getUf())
                        .situacaoIes(registro.getSituacaoIes())
                        .build();
                universidadeRepository.save(universidadeAlterada);
                return ResponseHandler.generateResponse("Universidade atualizada com súcesso!", HttpStatus.CREATED, universidadeAlterada);
            }
            return ResponseHandler.generateResponse("Erro ao encontrar a universidade!", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<Object> deletarUniversidade(UUID idUniversidade) {
        //try {
        //    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        //  if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
        //    return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        // }
        // Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
        //     Optional<Universidade> universidade = universidadeRepository.findById(idUniversidade);
//
        //          if (usuario.verificarUsuarioNaoEAdministrador()) {
        //            return ResponseHandler.generateResponse("Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
        //      }
        //    if (universidade.isPresent()) {
        //      universidadeRepository.deleteById(idUniversidade);
        //} else {
        //            return ResponseHandler.generateResponse("Não é possível excluir uma universidade que não existente.", HttpStatus.NOT_FOUND);
        //      }
        //    return ResponseHandler.generateResponse("Deletado com sucesso", HttpStatus.OK);
        //       } catch (Exception e) {
        //         return ResponseHandler.generateResponse("Houve um erro ao tentar excluir a universidade.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        //   }
        return ResponseHandler.generateResponse("Houve um erro ao tentar excluir a universidade.", HttpStatus.INTERNAL_SERVER_ERROR, "Não implementado ainda");
    }

    private boolean isUserAnonymous(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName());
    }

}
