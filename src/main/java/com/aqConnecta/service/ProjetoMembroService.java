package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.AlterarPapelRequest;
import com.aqConnecta.DTOs.request.ConviteRequest;
import com.aqConnecta.DTOs.response.ConviteResponse;
import com.aqConnecta.DTOs.response.MembroResponse;
import com.aqConnecta.DTOs.response.MembrosResponse;
import com.aqConnecta.DTOs.response.PublicadorResponse;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.exception.RecursoNaoEncontradoException;
import com.aqConnecta.model.Projeto;
import com.aqConnecta.model.ProjetoConvite;
import com.aqConnecta.model.ProjetoMembro;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.enums.PapelProjeto;
import com.aqConnecta.model.enums.StatusConvite;
import com.aqConnecta.repository.ProjetoConviteRepository;
import com.aqConnecta.repository.ProjetoMembroRepository;
import com.aqConnecta.repository.ProjetoRepository;
import com.aqConnecta.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjetoMembroService {

    private final ProjetoRepository projetoRepository;
    private final ProjetoMembroRepository projetoMembroRepository;
    private final ProjetoConviteRepository projetoConviteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProjetoAutorizacaoService autorizacaoService;
    private final UsuarioService usuarioService;
    private final EmailService emailService;

    @Value("${cors.urls:http://localhost:3000}")
    private String corsUrls;

    public ResponseEntity<Object> listarMembros(UUID idProjeto, String emailAutenticado, Pageable pageable) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeVisualizar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Este projeto é privado.", HttpStatus.FORBIDDEN);
            }
            List<MembroResponse> membros = projetoMembroRepository
                    .findByProjetoIdOrderByAtivoDescDataEntradaAsc(idProjeto, pageable)
                    .getContent().stream()
                    .map(this::membroToResponse)
                    .collect(Collectors.toList());
            MembrosResponse resposta = MembrosResponse.builder()
                    .dono(donoToResponse(projeto))
                    .membros(membros)
                    .build();
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, resposta);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao listar membros", e);
            return ResponseHandler.generateResponse("Houve um erro ao listar os membros.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> buscarUsuarios(UUID idProjeto, String q, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para convidar membros.", HttpStatus.FORBIDDEN);
            }
            if (q == null || q.trim().length() < 2) {
                return ResponseHandler.generateResponse("Informe ao menos 2 caracteres.", HttpStatus.OK, Collections.emptyList());
            }

            Set<UUID> excluir = new HashSet<>();
            if (projeto.getDono() != null) {
                excluir.add(projeto.getDono().getId());
            }
            projetoMembroRepository.findByProjetoIdAndAtivoTrue(idProjeto)
                    .forEach(m -> excluir.add(m.getUsuario().getId()));
            projetoConviteRepository.findByProjetoIdAndStatus(idProjeto, StatusConvite.PENDENTE)
                    .forEach(c -> excluir.add(c.getUsuarioConvidado().getId()));

            List<Usuario> encontrados = new ArrayList<>(usuarioRepository.findAllByNomeContainingIgnoreCase(q.trim()));
            usuarioRepository.findByEmail(q.trim()).ifPresent(u -> {
                if (encontrados.stream().noneMatch(e -> e.getId().equals(u.getId()))) {
                    encontrados.add(u);
                }
            });

            List<PublicadorResponse> resposta = encontrados.stream()
                    .filter(u -> !excluir.contains(u.getId()))
                    .filter(u -> !u.isDeleted())
                    .limit(10)
                    .map(u -> {
                        PublicadorResponse r = new PublicadorResponse();
                        r.inToOut(u);
                        return r;
                    })
                    .collect(Collectors.toList());
            return ResponseHandler.generateResponse("Busca feita com sucesso!", HttpStatus.OK, resposta);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao buscar usuários", e);
            return ResponseHandler.generateResponse("Houve um erro ao buscar usuários.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> convidar(UUID idProjeto, ConviteRequest req, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para convidar membros.", HttpStatus.FORBIDDEN);
            }
            if (req.getPapel() == null || req.getPapel() == PapelProjeto.DONO) {
                return ResponseHandler.generateResponse("Papel inválido. Use EDITOR ou VISUALIZADOR.", HttpStatus.BAD_REQUEST);
            }

            Usuario alvo = null;
            if (req.getIdUsuario() != null) {
                alvo = usuarioRepository.findById(req.getIdUsuario()).orElse(null);
            } else if (req.getEmail() != null && !req.getEmail().isBlank()) {
                alvo = usuarioRepository.findByEmail(req.getEmail().trim()).orElse(null);
            }
            if (alvo == null) {
                return ResponseHandler.generateResponse("Usuário não encontrado.", HttpStatus.NOT_FOUND);
            }
            if (projeto.getDono() != null && projeto.getDono().getId().equals(alvo.getId())) {
                return ResponseHandler.generateResponse("Este usuário já é o dono do projeto.", HttpStatus.BAD_REQUEST);
            }
            if (projetoMembroRepository.findByProjetoIdAndUsuarioIdAndAtivoTrue(idProjeto, alvo.getId()).isPresent()) {
                return ResponseHandler.generateResponse("Este usuário já é membro do projeto.", HttpStatus.CONFLICT);
            }
            if (projetoConviteRepository.findByProjetoIdAndUsuarioConvidadoIdAndStatus(idProjeto, alvo.getId(), StatusConvite.PENDENTE).isPresent()) {
                return ResponseHandler.generateResponse("Já existe um convite pendente para este usuário.", HttpStatus.CONFLICT);
            }

            ProjetoConvite convite = ProjetoConvite.builder()
                    .projeto(projeto)
                    .usuarioConvidado(alvo)
                    .convidadoPor(usuario)
                    .papel(req.getPapel())
                    .status(StatusConvite.PENDENTE)
                    .criadoEm(LocalDateTime.now())
                    .build();
            convite = projetoConviteRepository.saveAndFlush(convite);
            enviarEmailConvite(alvo, projeto, req.getPapel());
            return ResponseHandler.generateResponse("Convite enviado com sucesso!", HttpStatus.CREATED, conviteToResponse(convite));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao convidar membro", e);
            return ResponseHandler.generateResponse("Houve um erro ao enviar o convite.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listarConvitesProjeto(UUID idProjeto, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para ver os convites.", HttpStatus.FORBIDDEN);
            }
            List<ConviteResponse> resposta = projetoConviteRepository.findByProjetoIdAndStatus(idProjeto, StatusConvite.PENDENTE).stream()
                    .map(this::conviteToResponse)
                    .collect(Collectors.toList());
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, resposta);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao listar convites do projeto", e);
            return ResponseHandler.generateResponse("Houve um erro ao listar os convites.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listarMeusConvites(String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            List<ConviteResponse> resposta = projetoConviteRepository.findByUsuarioConvidadoIdAndStatus(usuario.getId(), StatusConvite.PENDENTE).stream()
                    .map(this::conviteToResponse)
                    .collect(Collectors.toList());
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, resposta);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao listar meus convites", e);
            return ResponseHandler.generateResponse("Houve um erro ao listar seus convites.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> aceitarConvite(UUID idConvite, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<ProjetoConvite> conviteOpt = projetoConviteRepository.findById(idConvite);
            if (conviteOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Convite não encontrado.", HttpStatus.NOT_FOUND);
            }
            ProjetoConvite convite = conviteOpt.get();
            if (convite.getUsuarioConvidado() == null || !convite.getUsuarioConvidado().getId().equals(usuario.getId())) {
                return ResponseHandler.generateResponse("Este convite não é seu.", HttpStatus.FORBIDDEN);
            }
            if (convite.getStatus() != StatusConvite.PENDENTE) {
                return ResponseHandler.generateResponse("Este convite já foi respondido.", HttpStatus.BAD_REQUEST);
            }
            convite.setStatus(StatusConvite.ACEITO);
            convite.setRespondidoEm(LocalDateTime.now());
            projetoConviteRepository.save(convite);

            Projeto projeto = convite.getProjeto();
            ProjetoMembro membro = projetoMembroRepository
                    .findByProjetoIdAndUsuarioId(projeto.getId(), usuario.getId())
                    .orElseGet(ProjetoMembro::new);
            membro.setProjeto(projeto);
            membro.setUsuario(usuario);
            membro.setPapel(convite.getPapel());
            membro.setAtivo(true);
            membro.setDataEntrada(LocalDateTime.now());
            membro.setDataSaida(null);
            projetoMembroRepository.save(membro);
            return ResponseHandler.generateResponse("Convite aceito! Você agora é membro do projeto.", HttpStatus.OK);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao aceitar convite", e);
            return ResponseHandler.generateResponse("Houve um erro ao aceitar o convite.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> recusarConvite(UUID idConvite, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<ProjetoConvite> conviteOpt = projetoConviteRepository.findById(idConvite);
            if (conviteOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Convite não encontrado.", HttpStatus.NOT_FOUND);
            }
            ProjetoConvite convite = conviteOpt.get();
            if (convite.getUsuarioConvidado() == null || !convite.getUsuarioConvidado().getId().equals(usuario.getId())) {
                return ResponseHandler.generateResponse("Este convite não é seu.", HttpStatus.FORBIDDEN);
            }
            if (convite.getStatus() != StatusConvite.PENDENTE) {
                return ResponseHandler.generateResponse("Este convite já foi respondido.", HttpStatus.BAD_REQUEST);
            }
            convite.setStatus(StatusConvite.RECUSADO);
            convite.setRespondidoEm(LocalDateTime.now());
            projetoConviteRepository.save(convite);
            return ResponseHandler.generateResponse("Convite recusado.", HttpStatus.OK);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao recusar convite", e);
            return ResponseHandler.generateResponse("Houve um erro ao recusar o convite.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> alterarPapel(UUID idProjeto, UUID idMembro, AlterarPapelRequest req, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para alterar membros.", HttpStatus.FORBIDDEN);
            }
            if (req.getPapel() == null || req.getPapel() == PapelProjeto.DONO) {
                return ResponseHandler.generateResponse("Papel inválido. Use EDITOR ou VISUALIZADOR.", HttpStatus.BAD_REQUEST);
            }
            Optional<ProjetoMembro> membroOpt = projetoMembroRepository.findById(idMembro);
            if (membroOpt.isEmpty() || membroOpt.get().getProjeto() == null
                    || !membroOpt.get().getProjeto().getId().equals(idProjeto)) {
                return ResponseHandler.generateResponse("Membro não encontrado.", HttpStatus.NOT_FOUND);
            }
            ProjetoMembro membro = membroOpt.get();
            membro.setPapel(req.getPapel());
            projetoMembroRepository.save(membro);
            return ResponseHandler.generateResponse("Papel atualizado com sucesso!", HttpStatus.OK, membroToResponse(membro));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao alterar papel", e);
            return ResponseHandler.generateResponse("Houve um erro ao alterar o papel.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> removerMembro(UUID idProjeto, UUID idMembro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para remover membros.", HttpStatus.FORBIDDEN);
            }
            Optional<ProjetoMembro> membroOpt = projetoMembroRepository.findById(idMembro);
            if (membroOpt.isEmpty() || membroOpt.get().getProjeto() == null
                    || !membroOpt.get().getProjeto().getId().equals(idProjeto)) {
                return ResponseHandler.generateResponse("Membro não encontrado.", HttpStatus.NOT_FOUND);
            }
            ProjetoMembro membro = membroOpt.get();
            membro.setAtivo(false);
            membro.setDataSaida(LocalDateTime.now());
            projetoMembroRepository.save(membro);
            return ResponseHandler.generateResponse("Membro removido do projeto.", HttpStatus.OK);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao remover membro", e);
            return ResponseHandler.generateResponse("Houve um erro ao remover o membro.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> reativarMembro(UUID idProjeto, UUID idMembro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para reativar membros.", HttpStatus.FORBIDDEN);
            }
            Optional<ProjetoMembro> membroOpt = projetoMembroRepository.findById(idMembro);
            if (membroOpt.isEmpty() || membroOpt.get().getProjeto() == null
                    || !membroOpt.get().getProjeto().getId().equals(idProjeto)) {
                return ResponseHandler.generateResponse("Membro não encontrado.", HttpStatus.NOT_FOUND);
            }
            ProjetoMembro membro = membroOpt.get();
            membro.setAtivo(true);
            membro.setDataSaida(null);
            membro.setDataEntrada(LocalDateTime.now());
            projetoMembroRepository.save(membro);
            return ResponseHandler.generateResponse("Membro reativado com sucesso!", HttpStatus.OK, membroToResponse(membro));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao reativar membro", e);
            return ResponseHandler.generateResponse("Houve um erro ao reativar o membro.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> transferir(UUID idProjeto, UUID idNovoDono, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.ehDono(projeto, usuario)) {
                return ResponseHandler.generateResponse("Apenas o dono pode transferir a propriedade.", HttpStatus.FORBIDDEN);
            }
            if (projeto.getDono() != null && projeto.getDono().getId().equals(idNovoDono)) {
                return ResponseHandler.generateResponse("Este usuário já é o dono.", HttpStatus.BAD_REQUEST);
            }
            Optional<ProjetoMembro> membroNovoOpt = projetoMembroRepository.findByProjetoIdAndUsuarioIdAndAtivoTrue(idProjeto, idNovoDono);
            if (membroNovoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("O novo dono precisa ser um membro ativo do projeto.", HttpStatus.BAD_REQUEST);
            }
            ProjetoMembro membroNovo = membroNovoOpt.get();
            Usuario novoDono = membroNovo.getUsuario();
            Usuario donoAntigo = projeto.getDono();

            projetoMembroRepository.delete(membroNovo);

            if (donoAntigo != null) {
                ProjetoMembro membroAntigo = projetoMembroRepository
                        .findByProjetoIdAndUsuarioId(idProjeto, donoAntigo.getId())
                        .orElseGet(ProjetoMembro::new);
                membroAntigo.setProjeto(projeto);
                membroAntigo.setUsuario(donoAntigo);
                membroAntigo.setPapel(PapelProjeto.EDITOR);
                membroAntigo.setAtivo(true);
                membroAntigo.setDataEntrada(LocalDateTime.now());
                membroAntigo.setDataSaida(null);
                projetoMembroRepository.save(membroAntigo);
            }

            projeto.setDono(novoDono);
            projeto.setAtualizadoEm(LocalDateTime.now());
            projetoRepository.save(projeto);
            return ResponseHandler.generateResponse("Propriedade transferida com sucesso.", HttpStatus.OK);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao transferir propriedade", e);
            return ResponseHandler.generateResponse("Houve um erro ao transferir a propriedade.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void enviarEmailConvite(Usuario alvo, Projeto projeto, PapelProjeto papel) {
        try {
            String base = corsUrls == null || corsUrls.isBlank() ? "" : corsUrls.split(",")[0].trim();
            String link = base + "/projetos/convites";
            String papelLabel = papel == PapelProjeto.EDITOR ? "Editor" : "Visualizador";
            String mensagem = "Você foi convidado para o projeto \"" + projeto.getTitulo()
                    + "\" como " + papelLabel + ". Acesse seus convites para aceitar ou recusar.";
            emailService.sendEmail(alvo.getEmail(), "Convite para projeto - AQConnecta",
                    emailService.criarCorpoEmail(alvo.getNome(), mensagem, link));
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail de convite: {}", e.getMessage());
        }
    }

    private MembroResponse membroToResponse(ProjetoMembro membro) {
        PublicadorResponse usuario = null;
        if (membro.getUsuario() != null) {
            usuario = new PublicadorResponse();
            usuario.inToOut(membro.getUsuario());
        }
        return MembroResponse.builder()
                .id(membro.getId())
                .usuario(usuario)
                .papel(membro.getPapel())
                .ativo(membro.getAtivo())
                .dataEntrada(membro.getDataEntrada())
                .dataSaida(membro.getDataSaida())
                .build();
    }

    private MembroResponse donoToResponse(Projeto projeto) {
        PublicadorResponse usuario = null;
        if (projeto.getDono() != null) {
            usuario = new PublicadorResponse();
            usuario.inToOut(projeto.getDono());
        }
        return MembroResponse.builder()
                .id(null)
                .usuario(usuario)
                .papel(PapelProjeto.DONO)
                .ativo(true)
                .dataEntrada(projeto.getCriadoEm())
                .dataSaida(null)
                .build();
    }

    private ConviteResponse conviteToResponse(ProjetoConvite convite) {
        PublicadorResponse usuario = null;
        if (convite.getUsuarioConvidado() != null) {
            usuario = new PublicadorResponse();
            usuario.inToOut(convite.getUsuarioConvidado());
        }
        Projeto projeto = convite.getProjeto();
        String capaUrl = null;
        if (projeto != null && projeto.getImagemCapa() != null) {
            capaUrl = "/projeto/" + projeto.getId() + "/midia/" + projeto.getImagemCapa();
        }
        return ConviteResponse.builder()
                .id(convite.getId())
                .projetoId(projeto != null ? projeto.getId() : null)
                .projetoTitulo(projeto != null ? projeto.getTitulo() : null)
                .projetoCapaUrl(capaUrl)
                .usuario(usuario)
                .convidadoPorNome(convite.getConvidadoPor() != null ? convite.getConvidadoPor().getNome() : null)
                .papel(convite.getPapel())
                .status(convite.getStatus())
                .criadoEm(convite.getCriadoEm())
                .build();
    }
}
