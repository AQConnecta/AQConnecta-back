package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.VagaRequest;
import com.aqConnecta.exception.base.AcaoProibidaException;
import com.aqConnecta.exception.base.NaoAutorizadoException;
import com.aqConnecta.exception.base.RecursoNaoEncontradoException;
import com.aqConnecta.exception.usuarios.UsuarioNaoVerificadoException;
import com.aqConnecta.exception.usuarios.UsuarioRemovidoException;
import com.aqConnecta.exception.vagas.JaSeCandidatouParaVagaException;
import com.aqConnecta.model.Candidatura;
import com.aqConnecta.model.Curriculo;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.Vaga;
import com.aqConnecta.repository.CandidaturaRepository;
import com.aqConnecta.repository.CurriculoRepository;
import com.aqConnecta.repository.VagaRepository;
import com.aqConnecta.repository.specs.VagaSpecs;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
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

    private boolean usuarioNaoPodeManipularVaga(Usuario usuario, Vaga vaga) {
        return !usuario.ehAdministrador() && !vaga.getPublicador().equals(usuario);
    }

    public Vaga cadastrarVaga(VagaRequest registro, Usuario usuario) {
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
        return vaga;
    }

    public List<Vaga> listarVagas(String titulo, UUID idCompetencia, Boolean iniciante) {
        this.assegurarQueUsuarioEstaAtivo();

        Specification<Vaga> spec = Specification.where(VagaSpecs.naoDeletada())
            .and(VagaSpecs.dentroDoPrazo())
            .and(VagaSpecs.possuiTitulo(titulo))
            .and(VagaSpecs.possuiCompetencia(idCompetencia))
            .and(VagaSpecs.isIniciante(iniciante));

        return vagaRepository.findAll(spec);
    }

    public Set<Vaga> listarVagasPorUsuario(Usuario usuario) {
        return vagaRepository.findByPublicador(usuario);
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

    public Vaga alterarVaga(UUID idVaga, VagaRequest registro, Usuario usuario) {
        final var vaga = vagaRepository
            .findById(idVaga)
            .orElseThrow(() -> new RecursoNaoEncontradoException(MessageFormat.format(
                "Não foi possível encontrar nenhuma vaga com id {0}.", idVaga)));

        if (this.usuarioNaoPodeManipularVaga(usuario, vaga)) {
            throw new AcaoProibidaException("Você não tem permissão para alterar este registro.");
        }

        var vagaAlterada = Vaga.builder()
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

        vagaAlterada = vagaRepository.save(vagaAlterada);
        return vagaAlterada;
    }

    public void deletarVaga(UUID idVaga, Usuario usuario) {
        vagaRepository.findById(idVaga).map(vaga -> {
            if (this.usuarioNaoPodeManipularVaga(usuario, vaga)) {
                throw new AcaoProibidaException("Você não tem permissão para remover esta vaga.");
            }

            vagaRepository.deleteById(vaga.getId());
            return Optional.empty();
        });
    }

    public Vaga candidatar(UUID vagaId, Integer curriculoId, Usuario usuario) {
        Vaga vaga = vagaRepository
            .findById(vagaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Vaga não existe"));

        Curriculo curriculo = curriculoRepository.getReferenceById(curriculoId);
        boolean jaCandidatado = vaga
            .getCandidaturas()
            .stream()
            .anyMatch(candidatura -> candidatura.getUsuario().getId().equals(usuario.getId()));

        if (jaCandidatado) throw new JaSeCandidatouParaVagaException(vaga);

        Candidatura novaCandidatura = Candidatura.builder()
            .usuario(usuario)
            .vaga(vaga)
            .curriculo(curriculo.getId())
            .curriculoUrl(curriculo.getCurriculo())
            .build();

        vaga.getCandidaturas().add(novaCandidatura);
        vaga = vagaRepository.save(vaga);

        return vaga;
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
