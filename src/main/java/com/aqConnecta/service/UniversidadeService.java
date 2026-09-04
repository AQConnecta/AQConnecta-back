package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.UniversidadeRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.model.Universidade;
import com.aqConnecta.repository.UniversidadeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UniversidadeService {

    @Autowired
    private UniversidadeRepository universidadeRepository;

    // SecurityConfig já protege com hasAuthority("ADMIN")
    public ResponseEntity<Object> cadastrarUniversidade(UniversidadeRequest registro) {
        try {
            Universidade universidade = Universidade.builder()
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
            universidadeRepository.save(universidade);
            return ResponseHandler.generateResponse("Universidade cadastrada com sucesso!", HttpStatus.CREATED, universidade);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> listarUniversidades(String search, Pageable pageable) {
        try {
            List<Universidade> universidades = (search == null || search.isBlank())
                    ? universidadeRepository.findAll(pageable).getContent()
                    : universidadeRepository
                        .findByNomeInstituicaoContainingIgnoreCaseOrSiglaContainingIgnoreCase(search.trim(), search.trim(), pageable)
                        .getContent();
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, universidades);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar as universidades.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> localizarUniversidade(UUID idUniversidade) {
        try {
            Optional<Universidade> universidade = universidadeRepository.findById(idUniversidade);
            if (universidade.isPresent()) {
                return ResponseHandler.generateResponse("Localizado com sucesso", HttpStatus.OK, universidade);
            }
            return ResponseHandler.generateResponse("Nenhuma universidade encontrada para este ID.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar localizar a universidade.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> alterarUniversidade(UUID idUniversidade, UniversidadeRequest registro) {
        try {
            Optional<Universidade> universidade = universidadeRepository.findById(idUniversidade);
            if (universidade.isPresent()) {
                Universidade universidadeAlterada = Universidade.builder()
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
                return ResponseHandler.generateResponse("Universidade atualizada com sucesso!", HttpStatus.OK, universidadeAlterada);
            }
            return ResponseHandler.generateResponse("Erro ao encontrar a universidade!", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> deletarUniversidade(UUID idUniversidade) {
        try {
            Optional<Universidade> universidade = universidadeRepository.findById(idUniversidade);
            if (universidade.isPresent()) {
                universidadeRepository.deleteById(idUniversidade);
                return ResponseHandler.generateResponse("Deletado com sucesso", HttpStatus.OK);
            }
            return ResponseHandler.generateResponse("Não é possível excluir uma universidade não existente.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar excluir a universidade.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
