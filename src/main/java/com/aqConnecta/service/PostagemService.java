package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.PostagemRequest;
import com.aqConnecta.DTOs.response.PostagemResponse;
import com.aqConnecta.DTOs.response.PostagemResumoResponse;
import com.aqConnecta.DTOs.response.ProjetoImagemResponse;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.exception.RecursoNaoEncontradoException;
import com.aqConnecta.model.Postagem;
import com.aqConnecta.model.PostagemImagem;
import com.aqConnecta.model.Projeto;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.enums.StatusPostagem;
import com.aqConnecta.repository.ComentarioRepository;
import com.aqConnecta.repository.PostagemImagemRepository;
import com.aqConnecta.repository.PostagemRepository;
import com.aqConnecta.repository.ProjetoRepository;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostagemService {

    private static final String SUBPASTA_POSTAGENS = "postagens";

    private final PostagemRepository postagemRepository;
    private final PostagemImagemRepository postagemImagemRepository;
    private final ProjetoRepository projetoRepository;
    private final ComentarioRepository comentarioRepository;
    private final ProjetoAutorizacaoService autorizacaoService;
    private final DocumentoService documentoService;
    private final UsuarioService usuarioService;

    public ResponseEntity<Object> criar(UUID idProjeto, PostagemRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para criar posts.", HttpStatus.FORBIDDEN);
            }
            StatusPostagem status = registro.getStatus() != null ? registro.getStatus() : StatusPostagem.RASCUNHO;
            Postagem postagem = Postagem.builder()
                    .projeto(projeto)
                    .autor(usuario)
                    .titulo(registro.getTitulo())
                    .corpo(registro.getCorpo())
                    .status(status)
                    .publicadoEm(status == StatusPostagem.PUBLICADO ? LocalDateTime.now() : null)
                    .criadoEm(LocalDateTime.now())
                    .atualizadoEm(LocalDateTime.now())
                    .build();
            postagem = postagemRepository.saveAndFlush(postagem);
            return ResponseHandler.generateResponse("Post criado com sucesso!", HttpStatus.CREATED, montarDetalhe(postagem, projeto, usuario));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao criar post", e);
            return ResponseHandler.generateResponse("Houve um erro ao criar o post.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
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

    public ResponseEntity<Object> listar(UUID idProjeto, String emailAutenticado, Pageable pageable) {
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
            Page<Postagem> page = autorizacaoService.podeGerenciar(projeto, usuario)
                    ? postagemRepository.findByProjetoIdOrderByCriadoEmDesc(idProjeto, pageable)
                    : postagemRepository.findByProjetoIdAndStatusOrderByPublicadoEmDesc(idProjeto, StatusPostagem.PUBLICADO, pageable);
            List<PostagemResumoResponse> resposta = page.getContent().stream()
                    .map(this::montarResumo)
                    .collect(Collectors.toList());
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, resposta);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao listar posts", e);
            return ResponseHandler.generateResponse("Houve um erro ao listar os posts.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> localizar(UUID idProjeto, UUID idPost, String emailAutenticado) {
        try {
            Usuario usuario = resolverUsuarioOpcional(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            Optional<Postagem> postOpt = postagemRepository.findById(idPost);
            if (postOpt.isEmpty() || postOpt.get().getProjeto() == null
                    || !postOpt.get().getProjeto().getId().equals(idProjeto)) {
                return ResponseHandler.generateResponse("Post não encontrado.", HttpStatus.NOT_FOUND);
            }
            Postagem postagem = postOpt.get();
            if (postagem.getStatus() == StatusPostagem.RASCUNHO && !autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Post não encontrado.", HttpStatus.NOT_FOUND);
            }
            if (!autorizacaoService.podeVisualizar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Este projeto é privado.", HttpStatus.FORBIDDEN);
            }
            return ResponseHandler.generateResponse("Post encontrado!", HttpStatus.OK, montarDetalhe(postagem, projeto, usuario));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao localizar post", e);
            return ResponseHandler.generateResponse("Houve um erro ao localizar o post.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> alterar(UUID idProjeto, UUID idPost, PostagemRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para editar posts.", HttpStatus.FORBIDDEN);
            }
            Optional<Postagem> postOpt = postagemRepository.findById(idPost);
            if (postOpt.isEmpty() || postOpt.get().getProjeto() == null
                    || !postOpt.get().getProjeto().getId().equals(idProjeto)) {
                return ResponseHandler.generateResponse("Post não encontrado.", HttpStatus.NOT_FOUND);
            }
            Postagem postagem = postOpt.get();
            postagem.setTitulo(registro.getTitulo());
            postagem.setCorpo(registro.getCorpo());
            if (registro.getStatus() != null) {
                if (registro.getStatus() == StatusPostagem.PUBLICADO && postagem.getPublicadoEm() == null) {
                    postagem.setPublicadoEm(LocalDateTime.now());
                }
                postagem.setStatus(registro.getStatus());
            }
            postagem.setAtualizadoEm(LocalDateTime.now());
            postagem = postagemRepository.save(postagem);
            return ResponseHandler.generateResponse("Post atualizado com sucesso!", HttpStatus.OK, montarDetalhe(postagem, projeto, usuario));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao alterar post", e);
            return ResponseHandler.generateResponse("Houve um erro ao alterar o post.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> deletar(UUID idProjeto, UUID idPost, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para excluir posts.", HttpStatus.FORBIDDEN);
            }
            Optional<Postagem> postOpt = postagemRepository.findById(idPost);
            if (postOpt.isEmpty() || postOpt.get().getProjeto() == null
                    || !postOpt.get().getProjeto().getId().equals(idProjeto)) {
                return ResponseHandler.generateResponse("Post não encontrado.", HttpStatus.NOT_FOUND);
            }
            Postagem postagem = postOpt.get();
            postagem.setDeletadoEm(LocalDateTime.now());
            postagemRepository.save(postagem);
            return ResponseHandler.generateResponse("Post excluído com sucesso.", HttpStatus.OK);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao excluir post", e);
            return ResponseHandler.generateResponse("Houve um erro ao excluir o post.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> adicionarImagem(UUID idProjeto, UUID idPost, MultipartFile file, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para alterar este post.", HttpStatus.FORBIDDEN);
            }
            Optional<Postagem> postOpt = postagemRepository.findById(idPost);
            if (postOpt.isEmpty() || postOpt.get().getProjeto() == null
                    || !postOpt.get().getProjeto().getId().equals(idProjeto)) {
                return ResponseHandler.generateResponse("Post não encontrado.", HttpStatus.NOT_FOUND);
            }
            Postagem postagem = postOpt.get();
            String filename = documentoService.uploadEmSubpasta(file, SUBPASTA_POSTAGENS);
            long total = postagemImagemRepository.countByPostagemId(idPost);
            PostagemImagem imagem = PostagemImagem.builder()
                    .postagem(postagem)
                    .caminho(filename)
                    .ordem((int) total)
                    .criadoEm(LocalDateTime.now())
                    .build();
            imagem = postagemImagemRepository.saveAndFlush(imagem);
            return ResponseHandler.generateResponse("Imagem adicionada ao post.", HttpStatus.CREATED, montarImagemResponse(idProjeto, idPost, imagem));
        } catch (IllegalArgumentException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao adicionar imagem ao post", e);
            return ResponseHandler.generateResponse("Houve um erro ao enviar a imagem.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> removerImagem(UUID idProjeto, UUID idPost, UUID idImagem, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (!autorizacaoService.podeGerenciar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem permissão para alterar este post.", HttpStatus.FORBIDDEN);
            }
            Optional<PostagemImagem> imagemOpt = postagemImagemRepository.findById(idImagem);
            if (imagemOpt.isEmpty() || imagemOpt.get().getPostagem() == null
                    || !imagemOpt.get().getPostagem().getId().equals(idPost)) {
                return ResponseHandler.generateResponse("Imagem não encontrada.", HttpStatus.NOT_FOUND);
            }
            PostagemImagem imagem = imagemOpt.get();
            postagemImagemRepository.delete(imagem);
            documentoService.removerArquivo(SUBPASTA_POSTAGENS, imagem.getCaminho());
            return ResponseHandler.generateResponse("Imagem removida.", HttpStatus.OK);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao remover imagem do post", e);
            return ResponseHandler.generateResponse("Houve um erro ao remover a imagem.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<?> carregarMidia(UUID idProjeto, UUID idPost, String filename, String emailAutenticado) {
        Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
        if (projetoOpt.isEmpty()) {
            return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
        }
        Projeto projeto = projetoOpt.get();
        Optional<Postagem> postOpt = postagemRepository.findById(idPost);
        if (postOpt.isEmpty() || postOpt.get().getProjeto() == null
                || !postOpt.get().getProjeto().getId().equals(idProjeto)) {
            return ResponseHandler.generateResponse("Post não encontrado.", HttpStatus.NOT_FOUND);
        }
        Postagem postagem = postOpt.get();
        Usuario usuario = null;
        try {
            usuario = usuarioService.localizarPorEmail(emailAutenticado);
        } catch (Exception ignored) {
        }
        boolean permitido = postagem.getStatus() == StatusPostagem.RASCUNHO
                ? autorizacaoService.podeGerenciar(projeto, usuario)
                : autorizacaoService.podeVisualizar(projeto, usuario);
        if (!permitido) {
            return ResponseHandler.generateResponse("Conteúdo restrito.", HttpStatus.FORBIDDEN);
        }
        try {
            Path arquivo = documentoService.resolverArquivo(SUBPASTA_POSTAGENS, filename);
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
            log.error("Erro ao servir mídia do post", e);
            return ResponseHandler.generateResponse("Erro ao carregar arquivo.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private PostagemResumoResponse montarResumo(Postagem postagem) {
        PostagemResumoResponse resumo = new PostagemResumoResponse();
        resumo.inToOut(postagem);
        List<PostagemImagem> imagens = postagemImagemRepository.findByPostagemIdOrderByOrdemAsc(postagem.getId());
        if (!imagens.isEmpty()) {
            resumo.setCapaUrl(montarUrlMidia(postagem.getProjeto().getId(), postagem.getId(), imagens.get(0).getCaminho()));
        }
        resumo.setTotalComentarios(comentarioRepository.countByPostagemId(postagem.getId()));
        return resumo;
    }

    private PostagemResponse montarDetalhe(Postagem postagem, Projeto projeto, Usuario usuarioAtual) {
        PostagemResponse resposta = new PostagemResponse();
        resposta.inToOut(postagem);
        List<ProjetoImagemResponse> imagens = postagemImagemRepository.findByPostagemIdOrderByOrdemAsc(postagem.getId()).stream()
                .map(img -> montarImagemResponse(projeto.getId(), postagem.getId(), img))
                .collect(Collectors.toList());
        resposta.setImagens(imagens);
        resposta.setTotalComentarios(comentarioRepository.countByPostagemId(postagem.getId()));
        resposta.setPodeEditar(autorizacaoService.podeGerenciar(projeto, usuarioAtual));
        return resposta;
    }

    private ProjetoImagemResponse montarImagemResponse(UUID idProjeto, UUID idPost, PostagemImagem imagem) {
        return ProjetoImagemResponse.builder()
                .id(imagem.getId())
                .url(montarUrlMidia(idProjeto, idPost, imagem.getCaminho()))
                .ordem(imagem.getOrdem())
                .build();
    }

    private String montarUrlMidia(UUID idProjeto, UUID idPost, String filename) {
        return "/projeto/" + idProjeto + "/posts/" + idPost + "/midia/" + filename;
    }
}
