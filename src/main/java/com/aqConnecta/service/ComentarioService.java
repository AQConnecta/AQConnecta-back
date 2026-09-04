package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.ComentarioRequest;
import com.aqConnecta.DTOs.response.ComentarioResponse;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.exception.RecursoNaoEncontradoException;
import com.aqConnecta.model.Comentario;
import com.aqConnecta.model.Postagem;
import com.aqConnecta.model.Projeto;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.enums.StatusPostagem;
import com.aqConnecta.repository.ComentarioRepository;
import com.aqConnecta.repository.PostagemRepository;
import com.aqConnecta.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PostagemRepository postagemRepository;
    private final ProjetoRepository projetoRepository;
    private final ProjetoAutorizacaoService autorizacaoService;
    private final UsuarioService usuarioService;

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

    public ResponseEntity<Object> listar(UUID idProjeto, UUID idPost, String emailAutenticado, Pageable pageable) {
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
            Postagem post = postOpt.get();
            boolean gerencia = autorizacaoService.podeGerenciar(projeto, usuario);
            if (post.getStatus() == StatusPostagem.RASCUNHO && !gerencia) {
                return ResponseHandler.generateResponse("Post não encontrado.", HttpStatus.NOT_FOUND);
            }
            if (!autorizacaoService.podeVisualizar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Este projeto é privado.", HttpStatus.FORBIDDEN);
            }

            List<Comentario> topo = comentarioRepository
                    .findByPostagemIdAndComentarioPaiIsNullOrderByCriadoEmDesc(idPost, pageable)
                    .getContent();
            List<UUID> paiIds = topo.stream().map(Comentario::getId).collect(Collectors.toList());
            Map<UUID, List<Comentario>> respostasPorPai = new HashMap<>();
            if (!paiIds.isEmpty()) {
                for (Comentario r : comentarioRepository.findByComentarioPaiIdInOrderByCriadoEmAsc(paiIds)) {
                    if (r.getComentarioPai() != null) {
                        respostasPorPai.computeIfAbsent(r.getComentarioPai().getId(), k -> new ArrayList<>()).add(r);
                    }
                }
            }
            List<ComentarioResponse> resposta = topo.stream().map(c -> {
                ComentarioResponse cr = montar(c, usuario, gerencia);
                List<ComentarioResponse> filhos = respostasPorPai.getOrDefault(c.getId(), new ArrayList<>())
                        .stream().map(f -> montar(f, usuario, gerencia)).collect(Collectors.toList());
                cr.setRespostas(filhos);
                return cr;
            }).collect(Collectors.toList());
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, resposta);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao listar comentários", e);
            return ResponseHandler.generateResponse("Houve um erro ao listar os comentários.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> criar(UUID idProjeto, UUID idPost, ComentarioRequest req, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
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
            Postagem post = postOpt.get();
            boolean gerencia = autorizacaoService.podeGerenciar(projeto, usuario);
            if (post.getStatus() == StatusPostagem.RASCUNHO && !gerencia) {
                return ResponseHandler.generateResponse("Post não encontrado.", HttpStatus.NOT_FOUND);
            }
            if (!autorizacaoService.podeVisualizar(projeto, usuario)) {
                return ResponseHandler.generateResponse("Você não tem acesso a este post.", HttpStatus.FORBIDDEN);
            }

            Comentario pai = null;
            if (req.getIdComentarioPai() != null) {
                pai = comentarioRepository.findById(req.getIdComentarioPai()).orElse(null);
                if (pai == null || pai.getPostagem() == null || !pai.getPostagem().getId().equals(idPost)) {
                    return ResponseHandler.generateResponse("Comentário pai inválido.", HttpStatus.BAD_REQUEST);
                }
                if (pai.getComentarioPai() != null) {
                    return ResponseHandler.generateResponse("Respostas possuem apenas 1 nível.", HttpStatus.BAD_REQUEST);
                }
            }

            Comentario comentario = Comentario.builder()
                    .postagem(post)
                    .autor(usuario)
                    .comentarioPai(pai)
                    .corpo(req.getCorpo())
                    .criadoEm(LocalDateTime.now())
                    .build();
            comentario = comentarioRepository.saveAndFlush(comentario);
            return ResponseHandler.generateResponse("Comentário publicado!", HttpStatus.CREATED, montar(comentario, usuario, gerencia));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao criar comentário", e);
            return ResponseHandler.generateResponse("Houve um erro ao publicar o comentário.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> deletar(UUID idProjeto, UUID idPost, UUID idComentario, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Comentario> comentarioOpt = comentarioRepository.findById(idComentario);
            if (comentarioOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Comentário não encontrado.", HttpStatus.NOT_FOUND);
            }
            Comentario comentario = comentarioOpt.get();
            Postagem post = comentario.getPostagem();
            if (post == null || !post.getId().equals(idPost) || post.getProjeto() == null
                    || !post.getProjeto().getId().equals(idProjeto)) {
                return ResponseHandler.generateResponse("Comentário não encontrado.", HttpStatus.NOT_FOUND);
            }
            boolean autor = comentario.getAutor() != null && comentario.getAutor().getId().equals(usuario.getId());
            if (!autor && !autorizacaoService.podeGerenciar(post.getProjeto(), usuario)) {
                return ResponseHandler.generateResponse("Você não pode excluir este comentário.", HttpStatus.FORBIDDEN);
            }
            LocalDateTime agora = LocalDateTime.now();
            comentario.setDeletadoEm(agora);
            comentarioRepository.save(comentario);
            if (comentario.getComentarioPai() == null) {
                comentarioRepository.findByComentarioPaiId(comentario.getId()).forEach(r -> {
                    r.setDeletadoEm(agora);
                    comentarioRepository.save(r);
                });
            }
            return ResponseHandler.generateResponse("Comentário removido.", HttpStatus.OK);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao excluir comentário", e);
            return ResponseHandler.generateResponse("Houve um erro ao excluir o comentário.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private ComentarioResponse montar(Comentario comentario, Usuario usuario, boolean gerencia) {
        ComentarioResponse cr = new ComentarioResponse();
        cr.inToOut(comentario);
        boolean autor = comentario.getAutor() != null && usuario != null
                && comentario.getAutor().getId().equals(usuario.getId());
        cr.setPodeDeletar(autor || gerencia);
        return cr;
    }
}
