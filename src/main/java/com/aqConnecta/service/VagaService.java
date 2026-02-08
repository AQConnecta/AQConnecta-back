package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.VagaRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.DTOs.response.VagaResponse;
import com.aqConnecta.exception.base.AcaoProibidaException;
import com.aqConnecta.exception.base.NaoAutorizadoException;
import com.aqConnecta.exception.base.RecursoNaoEncontradoException;
import com.aqConnecta.exception.usuarios.UsuarioNaoVerificadoException;
import com.aqConnecta.exception.usuarios.UsuarioRemovidoException;
import com.aqConnecta.model.Candidatura;
import com.aqConnecta.model.Curriculo;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.Vaga;
import com.aqConnecta.repository.CandidaturaRepository;
import com.aqConnecta.repository.CurriculoRepository;
import com.aqConnecta.repository.VagaRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VagaService {

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private CandidaturaRepository candidaturaRepository;

    @Autowired
    private CurriculoRepository curriculoRepository;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Assegura que o usuário da requisição está logado e é um usuário ativo no sistema, com
     * e-mail confirmado ou não.
     */
    private void assegurarQueUsuarioEstaAtivo() throws NaoAutorizadoException {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            var _usuario = this.usuarioService.obterDaAutenticacao(authentication);
        }
        catch (UsuarioNaoVerificadoException ignored) {
        }
        catch (UsuarioRemovidoException e) {
            throw new NaoAutorizadoException("Você não tem permissão para visualizar as vagas.", e);
        }
    }

    public ResponseEntity<Object> cadastrarVaga(VagaRequest registro, Usuario usuario) {
        Vaga vaga = Vaga.builder()
            .publicador(usuario)
            .titulo(registro.getTitulo())
            .descricao(registro.getDescricao())
            .localDaVaga(registro.getLocalDaVaga())
            .aceitaRemoto(registro.isAceitaRemoto())
            .dataLimiteCandidatura(registro.getDataLimiteCandidatura())
            .isIniciante(registro.isIniciante())
            .build();

        vaga = vagaRepository.save(vaga);
        return ResponseHandler.generateResponse("Vaga cadastrada com sucesso!", HttpStatus.CREATED, vaga);
    }

    public List<VagaResponse> fillVagaResponse(@NonNull List<Vaga> vagas) {
        List<VagaResponse> vagasResponse = new ArrayList<>();
        for (Vaga vaga : vagas) {
            VagaResponse vagaResponse = new VagaResponse();
            vagaResponse.inToOut(vaga);
            vagasResponse.add(vagaResponse);
        }
        return vagasResponse;
    }

    // TODO: Implementar os filtros no repositório
    public ResponseEntity<Object> listarVagas(String titulo, UUID idCompetencia, Boolean iniciante) {
        this.assegurarQueUsuarioEstaAtivo();

        LocalDateTime now = LocalDateTime.now();
        List<Vaga> vagas;

        // Filtra por título, competência ou todas as vagas
        if (!Strings.isEmpty(titulo)) {
            vagas = vagaRepository.findByTituloContainingIgnoreCase(titulo);
        }
        else if (idCompetencia != null) {
            vagas = vagaRepository.findByCompetenciaId(idCompetencia);
        }
        else {
            vagas = vagaRepository.findAll();
        }

        // Aplica os filtros de deletado e data limite
        vagas = vagas
            .stream()
            .filter(vaga ->
                vaga.getDeletadoEm() == null &&
                (vaga.getDataLimiteCandidatura() == null || vaga.getDataLimiteCandidatura().isAfter(now)))
            .toList();

        // Aplica o filtro de "iniciante" se o parâmetro foi fornecido
        if (iniciante != null) {
            vagas = vagas.stream()
                .filter(vaga -> vaga.isIniciante() == iniciante) // Filtra baseado no campo isIniciante
                .collect(Collectors.toList());
        }

        // Gera a resposta
        List<VagaResponse> vagasResponse = fillVagaResponse(vagas);
        return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, vagasResponse);
    }

    public ResponseEntity<Object> listarVagasPorUsuario(Usuario usuario) {
        Set<Vaga> vagas = vagaRepository.findByPublicador(usuario);

        List<VagaResponse> vagasResponse = fillVagaResponse(vagas.stream().toList());
        if (!vagasResponse.isEmpty()) {
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, vagasResponse);
        }
        return ResponseHandler.generateResponse("Nenhum vaga encontrada para este usuário.", HttpStatus.NO_CONTENT);
    }

    public Vaga localizar(UUID idVaga) throws RecursoNaoEncontradoException {
        this.assegurarQueUsuarioEstaAtivo();

        Vaga vaga = vagaRepository
            .findById(idVaga)
            .orElseThrow(() -> new RecursoNaoEncontradoException(MessageFormat.format(
                "Nenhuma vaga não encontrado com id \"{0}\".", idVaga)));

        if (vaga.getDeletadoEm() != null) {
            throw new RecursoNaoEncontradoException("Esta vaga não existe mais.");
        }

        return vaga;
    }

    public ResponseEntity<Object> alterarVaga(UUID idVaga, VagaRequest registro, Usuario usuario) {
        Optional<Vaga> vaga = vagaRepository.findById(idVaga);
        if (vaga.isPresent()) {
            if (!vaga.get().getPublicador().getId().equals(usuario.getId())
                && usuario.verificarUsuarioNaoEAdministrador()) {
                return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.",
                    HttpStatus.UNAUTHORIZED);
            }
            Vaga vagaAlterada = Vaga.builder()
                .id(idVaga)
                .publicador(usuario)
                .titulo(registro.getTitulo())
                .descricao(registro.getDescricao())
                .localDaVaga(registro.getLocalDaVaga())
                .aceitaRemoto(registro.isAceitaRemoto())
                .dataLimiteCandidatura(registro.getDataLimiteCandidatura())
                .atualizadoEm(LocalDateTime.now())
                .isIniciante(registro.isIniciante())
                .build();
            vagaRepository.save(vagaAlterada);
            return ResponseHandler.generateResponse("Vaga atualizada com súcesso!", HttpStatus.OK, vaga);
        }
        return ResponseHandler.generateResponse("Erro ao encontrar a vaga!", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Object> deletarVaga(UUID idVaga, Usuario usuario) {
        Optional<Vaga> vaga = vagaRepository.findById(idVaga);

        if (vaga.isPresent()) {
            if (!vaga.get().getPublicador().getId().equals(usuario.getId())
                && usuario.verificarUsuarioNaoEAdministrador()) {
                return ResponseHandler.generateResponse("Você não tem permissão para alterar esse registro.",
                    HttpStatus.FORBIDDEN);
            }
            vagaRepository.deleteById(idVaga);
            return ResponseHandler.generateResponse("Deletado com sucesso", HttpStatus.OK);
        }
        else {
            return ResponseHandler.generateResponse("Não é possível excluir uma vaga não existente.",
                HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Object> candidatar(UUID vagaId, Integer curriculoId, Usuario usuario) {
        Vaga vaga = vagaRepository
            .findById(vagaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Vaga não existe"));

        Curriculo curriculo = curriculoRepository.getReferenceById(curriculoId);
        boolean jaCandidatado = vaga
            .getCandidaturas()
            .stream()
            .anyMatch(candidatura -> candidatura.getUsuario().getId().equals(usuario.getId()));

        if (jaCandidatado) {
            return ResponseHandler.generateResponse("Você já se candidatou a esta vaga.", HttpStatus.BAD_REQUEST);
        }

        Candidatura novaCandidatura = Candidatura.builder()
            .usuario(usuario)
            .vaga(vaga)
            .curriculo(curriculoId)
            .curriculoUrl(curriculo.getCurriculo())
            .build();

        vaga.getCandidaturas().add(novaCandidatura);

        vaga = vagaRepository.save(vaga);

        return ResponseHandler.generateResponse("Candidatura enviada com sucesso", HttpStatus.OK, vaga);
    }

    public List<Candidatura> listarCandidaturas(UUID vagaId, Usuario usuario) {
        Vaga vaga = vagaRepository
            .findById(vagaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Vaga não existe."));

        boolean usuarioEhAutor = usuario.getId().equals(vaga.getPublicador().getId());
        boolean usuarioPodeVisualizarCandidaturas = usuarioEhAutor || !usuario.verificarUsuarioNaoEAdministrador();

        if (!usuarioPodeVisualizarCandidaturas) {
            throw new AcaoProibidaException("Você não tem permissão para visualizar as candidaturas desta vaga.");
        }

        return candidaturaRepository.findAllCandidaturaByVagaId(vaga.getId());
    }
}
