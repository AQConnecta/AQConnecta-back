package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.ProjetoLinkRequest;
import com.aqConnecta.DTOs.request.ProjetoRequest;
import com.aqConnecta.DTOs.response.ProjetoImagemResponse;
import com.aqConnecta.DTOs.response.ProjetoLinkResponse;
import com.aqConnecta.DTOs.response.ProjetoResponse;
import com.aqConnecta.DTOs.response.ProjetoResumoResponse;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.exception.RecursoNaoEncontradoException;
import com.aqConnecta.model.Area;
import com.aqConnecta.model.Projeto;
import com.aqConnecta.model.ProjetoImagem;
import com.aqConnecta.model.ProjetoLink;
import com.aqConnecta.model.ProjetoSeguidor;
import com.aqConnecta.model.Universidade;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.enums.StatusProjeto;
import com.aqConnecta.model.enums.VisibilidadeProjeto;
import com.aqConnecta.repository.AreaRepository;
import com.aqConnecta.repository.ProjetoImagemRepository;
import com.aqConnecta.repository.ProjetoLinkRepository;
import com.aqConnecta.repository.ProjetoMembroRepository;
import com.aqConnecta.repository.ProjetoRepository;
import com.aqConnecta.repository.ProjetoSeguidorRepository;
import com.aqConnecta.repository.UniversidadeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjetoService {

    private static final String SUBPASTA_PROJETOS = "projetos";

    private final ProjetoRepository projetoRepository;
    private final ProjetoImagemRepository projetoImagemRepository;
    private final ProjetoMembroRepository projetoMembroRepository;
    private final AreaRepository areaRepository;
    private final UniversidadeRepository universidadeRepository;
    private final ProjetoLinkRepository projetoLinkRepository;
    private final ProjetoSeguidorRepository projetoSeguidorRepository;
    private final ProjetoAutorizacaoService autorizacaoService;
    private final DocumentoService documentoService;
    private final UsuarioService usuarioService;
    private final BusinessMetrics businessMetrics;

    public ResponseEntity<Object> cadastrar(ProjetoRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Area area = areaRepository.findById(registro.getIdArea()).orElse(null);
            if (area == null) {
                return ResponseHandler.generateResponse("Área informada não existe.", HttpStatus.BAD_REQUEST);
            }
            Projeto projeto = Projeto.builder()
                    .titulo(registro.getTitulo())
                    .descricao(registro.getDescricao())
                    .area(area)
                    .universidade(resolverUniversidade(registro.getIdUniversidade()))
                    .status(registro.getStatus() != null ? registro.getStatus() : StatusProjeto.ATIVO)
                    .visibilidade(registro.getVisibilidade() != null ? registro.getVisibilidade() : VisibilidadeProjeto.PUBLICO)
                    .dono(usuario)
                    .criadoEm(LocalDateTime.now())
                    .atualizadoEm(LocalDateTime.now())
                    .build();
            projeto = projetoRepository.saveAndFlush(projeto);
            sincronizarLinks(projeto, registro.getLinks());
            businessMetrics.projetoCriado();
            return ResponseHandler.generateResponse("Projeto criado com sucesso!", HttpStatus.CREATED, montarDetalhe(projeto, usuario));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao criar projeto", e);
            return ResponseHandler.generateResponse("Houve um erro ao criar o projeto.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listarPublicos(String titulo, UUID idArea, StatusProjeto status, Pageable pageable) {
        try {
            Page<Projeto> page = projetoRepository.listarPublicos(titulo == null ? "" : titulo, idArea, status, pageable);
            List<ProjetoResumoResponse> resposta = page.getContent().stream()
                    .map(this::montarResumo)
                    .collect(Collectors.toList());
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, resposta);
        } catch (Exception e) {
            log.error("Erro ao listar projetos", e);
            return ResponseHandler.generateResponse("Houve um erro ao listar os projetos.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listarMeus(String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            List<ProjetoResumoResponse> resposta = projetoRepository.findMeusProjetos(usuario.getId()).stream()
                    .map(this::montarResumo)
                    .collect(Collectors.toList());
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, resposta);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao listar meus projetos", e);
            return ResponseHandler.generateResponse("Houve um erro ao listar seus projetos.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> localizar(UUID idProjeto, String emailAutenticado) {
        try {
            Usuario usuario = resolverUsuarioOpcional(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeVisualizar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Este projeto é privado.", HttpStatus.FORBIDDEN);
            }
            return ResponseHandler.generateResponse("Projeto encontrado!", HttpStatus.OK, montarDetalhe(projeto, usuario));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao localizar projeto", e);
            return ResponseHandler.generateResponse("Houve um erro ao localizar o projeto.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Usuario resolverUsuarioOpcional(String emailAutenticado) {
        if (emailAutenticado == null) {
            return null;
        }
        try {
            return usuarioService.localizarPorEmail(emailAutenticado);
        } catch (Exception e) {
            return null;
        }
    }

    public ResponseEntity<Object> alterar(UUID idProjeto, ProjetoRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para editar este projeto.", HttpStatus.FORBIDDEN);
            }
            Area area = areaRepository.findById(registro.getIdArea()).orElse(null);
            if (area == null) {
                return ResponseHandler.generateResponse("Área informada não existe.", HttpStatus.BAD_REQUEST);
            }
            projeto.setTitulo(registro.getTitulo());
            projeto.setDescricao(registro.getDescricao());
            projeto.setArea(area);
            projeto.setUniversidade(resolverUniversidade(registro.getIdUniversidade()));
            if (registro.getStatus() != null) projeto.setStatus(registro.getStatus());
            if (registro.getVisibilidade() != null) projeto.setVisibilidade(registro.getVisibilidade());
            projeto.setAtualizadoEm(LocalDateTime.now());
            projeto = projetoRepository.save(projeto);
            sincronizarLinks(projeto, registro.getLinks());
            return ResponseHandler.generateResponse("Projeto atualizado com sucesso!", HttpStatus.OK, montarDetalhe(projeto, usuario));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao alterar projeto", e);
            return ResponseHandler.generateResponse("Houve um erro ao alterar o projeto.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> deletar(UUID idProjeto, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.ehDono(projeto, usuario)) {
                return ResponseHandler.generateResponse("Apenas o dono pode excluir o projeto.", HttpStatus.FORBIDDEN);
            }
            projeto.setDeletadoEm(LocalDateTime.now());
            projetoRepository.save(projeto);
            return ResponseHandler.generateResponse("Projeto excluído com sucesso.", HttpStatus.OK);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao excluir projeto", e);
            return ResponseHandler.generateResponse("Houve um erro ao excluir o projeto.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> uploadCapa(UUID idProjeto, MultipartFile file, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para alterar este projeto.", HttpStatus.FORBIDDEN);
            }
            String anterior = projeto.getImagemCapa();
            String filename = documentoService.uploadEmSubpasta(file, SUBPASTA_PROJETOS);
            projeto.setImagemCapa(filename);
            projeto.setAtualizadoEm(LocalDateTime.now());
            projetoRepository.save(projeto);
            if (anterior != null && !anterior.isBlank()) {
                documentoService.removerArquivo(SUBPASTA_PROJETOS, anterior);
            }
            return ResponseHandler.generateResponse("Capa atualizada com sucesso.", HttpStatus.OK, montarUrlMidia(idProjeto, filename));
        } catch (IllegalArgumentException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao enviar capa do projeto", e);
            return ResponseHandler.generateResponse("Houve um erro ao enviar a imagem.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> adicionarImagemGaleria(UUID idProjeto, MultipartFile file, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para alterar este projeto.", HttpStatus.FORBIDDEN);
            }
            String filename = documentoService.uploadEmSubpasta(file, SUBPASTA_PROJETOS);
            long total = projetoImagemRepository.countByProjetoId(idProjeto);
            ProjetoImagem imagem = ProjetoImagem.builder()
                    .projeto(projeto)
                    .caminho(filename)
                    .ordem((int) total)
                    .criadoEm(LocalDateTime.now())
                    .build();
            imagem = projetoImagemRepository.saveAndFlush(imagem);
            return ResponseHandler.generateResponse("Imagem adicionada à galeria.", HttpStatus.CREATED, montarImagemResponse(idProjeto, imagem));
        } catch (IllegalArgumentException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao adicionar imagem à galeria", e);
            return ResponseHandler.generateResponse("Houve um erro ao enviar a imagem.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> removerImagemGaleria(UUID idProjeto, UUID idImagem, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para alterar este projeto.", HttpStatus.FORBIDDEN);
            }
            Optional<ProjetoImagem> imagemOpt = projetoImagemRepository.findById(idImagem);
            if (imagemOpt.isEmpty() || imagemOpt.get().getProjeto() == null
                    || !imagemOpt.get().getProjeto().getId().equals(idProjeto)) {
                return ResponseHandler.generateResponse("Imagem não encontrada.", HttpStatus.NOT_FOUND);
            }
            ProjetoImagem imagem = imagemOpt.get();
            projetoImagemRepository.delete(imagem);
            documentoService.removerArquivo(SUBPASTA_PROJETOS, imagem.getCaminho());
            return ResponseHandler.generateResponse("Imagem removida.", HttpStatus.OK);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao remover imagem da galeria", e);
            return ResponseHandler.generateResponse("Houve um erro ao remover a imagem.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<?> carregarMidia(UUID idProjeto, String filename, String emailAutenticado) {
        Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
        if (projetoOpt.isEmpty()) {
            return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
        }
        Projeto projeto = projetoOpt.get();
        if (projeto.getVisibilidade() == VisibilidadeProjeto.PRIVADO) {
            Usuario usuario = null;
            try {
                usuario = usuarioService.localizarPorEmail(emailAutenticado);
            } catch (Exception ignored) {
            }
            if (!autorizacaoService.podeVisualizar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Conteúdo restrito aos membros do projeto.", HttpStatus.FORBIDDEN);
            }
        }
        try {
            Path arquivo = documentoService.resolverArquivo(SUBPASTA_PROJETOS, filename);
            if (!Files.exists(arquivo) || !Files.isRegularFile(arquivo)) {
                return ResponseHandler.generateResponse("Arquivo não encontrado.", HttpStatus.NOT_FOUND);
            }
            Resource resource = new UrlResource(arquivo.toUri());
            String mime = Files.probeContentType(arquivo);
            MediaType mediaType = mime != null ? MediaType.parseMediaType(mime) : MediaType.APPLICATION_OCTET_STREAM;
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePrivate())
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Erro ao servir mídia do projeto", e);
            return ResponseHandler.generateResponse("Erro ao carregar arquivo.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> seguir(UUID idProjeto, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (projeto.getVisibilidade() != VisibilidadeProjeto.PUBLICO) {
                return ResponseHandler.generateResponse("Só é possível seguir projetos públicos.", HttpStatus.BAD_REQUEST);
            }
            if (!projetoSeguidorRepository.existsByProjetoIdAndUsuarioId(idProjeto, usuario.getId())) {
                ProjetoSeguidor seguidor = ProjetoSeguidor.builder()
                        .projeto(projeto)
                        .usuario(usuario)
                        .criadoEm(LocalDateTime.now())
                        .build();
                projetoSeguidorRepository.save(seguidor);
            }
            return ResponseHandler.generateResponse("Agora você segue este projeto.", HttpStatus.OK, projetoSeguidorRepository.countByProjetoId(idProjeto));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao seguir projeto", e);
            return ResponseHandler.generateResponse("Houve um erro ao seguir o projeto.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> deixarDeSeguir(UUID idProjeto, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            if (projetoRepository.findById(idProjeto).isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            projetoSeguidorRepository.findByProjetoIdAndUsuarioId(idProjeto, usuario.getId())
                    .ifPresent(projetoSeguidorRepository::delete);
            return ResponseHandler.generateResponse("Você deixou de seguir o projeto.", HttpStatus.OK, projetoSeguidorRepository.countByProjetoId(idProjeto));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao deixar de seguir projeto", e);
            return ResponseHandler.generateResponse("Houve um erro ao deixar de seguir o projeto.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listarSeguidos(String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            List<ProjetoResumoResponse> resposta = projetoSeguidorRepository.findByUsuarioIdOrderByCriadoEmDesc(usuario.getId()).stream()
                    .map(ProjetoSeguidor::getProjeto)
                    .filter(Objects::nonNull)
                    .map(this::montarResumo)
                    .collect(Collectors.toList());
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, resposta);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao listar projetos seguidos", e);
            return ResponseHandler.generateResponse("Houve um erro ao listar os projetos.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Universidade resolverUniversidade(UUID idUniversidade) {
        return idUniversidade == null ? null : universidadeRepository.findById(idUniversidade).orElse(null);
    }

    private void sincronizarLinks(Projeto projeto, List<ProjetoLinkRequest> links) {
        List<ProjetoLink> existentes = projetoLinkRepository.findByProjetoIdOrderByOrdemAsc(projeto.getId());
        if (!existentes.isEmpty()) {
            projetoLinkRepository.deleteAll(existentes);
        }
        if (links == null) {
            return;
        }
        int ordem = 0;
        for (ProjetoLinkRequest l : links) {
            if (l == null || l.getUrl() == null || l.getUrl().isBlank()) {
                continue;
            }
            ProjetoLink link = ProjetoLink.builder()
                    .projeto(projeto)
                    .titulo(l.getTitulo())
                    .url(l.getUrl().trim())
                    .ordem(ordem++)
                    .build();
            projetoLinkRepository.save(link);
        }
    }

    private ProjetoResumoResponse montarResumo(Projeto projeto) {
        ProjetoResumoResponse resumo = new ProjetoResumoResponse();
        resumo.inToOut(projeto);
        if (projeto.getImagemCapa() != null) {
            resumo.setCapaUrl(montarUrlMidia(projeto.getId(), projeto.getImagemCapa()));
        }
        return resumo;
    }

    private ProjetoResponse montarDetalhe(Projeto projeto, Usuario usuarioAtual) {
        ProjetoResponse resposta = new ProjetoResponse();
        resposta.inToOut(projeto);
        if (projeto.getImagemCapa() != null) {
            resposta.setCapaUrl(montarUrlMidia(projeto.getId(), projeto.getImagemCapa()));
        }
        List<ProjetoImagemResponse> imagens = projetoImagemRepository.findByProjetoIdOrderByOrdemAsc(projeto.getId()).stream()
                .map(img -> montarImagemResponse(projeto.getId(), img))
                .collect(Collectors.toList());
        resposta.setImagens(imagens);
        List<ProjetoLinkResponse> links = projetoLinkRepository.findByProjetoIdOrderByOrdemAsc(projeto.getId()).stream()
                .map(l -> ProjetoLinkResponse.builder().id(l.getId()).titulo(l.getTitulo()).url(l.getUrl()).build())
                .collect(Collectors.toList());
        resposta.setLinks(links);
        resposta.setTotalMembros(projetoMembroRepository.findByProjetoIdAndAtivoTrue(projeto.getId()).size() + 1L);
        resposta.setTotalSeguidores(projetoSeguidorRepository.countByProjetoId(projeto.getId()));
        resposta.setSeguindo(usuarioAtual != null && projetoSeguidorRepository.existsByProjetoIdAndUsuarioId(projeto.getId(), usuarioAtual.getId()));
        resposta.setPapelUsuarioAtual(autorizacaoService.papelDoUsuario(projeto, usuarioAtual));
        return resposta;
    }

    private ProjetoImagemResponse montarImagemResponse(UUID idProjeto, ProjetoImagem imagem) {
        return ProjetoImagemResponse.builder()
                .id(imagem.getId())
                .url(montarUrlMidia(idProjeto, imagem.getCaminho()))
                .ordem(imagem.getOrdem())
                .build();
    }

    private String montarUrlMidia(UUID idProjeto, String filename) {
        return "/projeto/" + idProjeto + "/midia/" + filename;
    }
}
