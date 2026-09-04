package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.AlterarStatusDenunciaRequest;
import com.aqConnecta.DTOs.request.DenunciaRequest;
import com.aqConnecta.DTOs.response.DenunciaResponse;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.exception.RecursoNaoEncontradoException;
import com.aqConnecta.model.Denuncia;
import com.aqConnecta.model.Permissao;
import com.aqConnecta.model.Projeto;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.enums.MotivoDenuncia;
import com.aqConnecta.model.enums.StatusDenuncia;
import com.aqConnecta.repository.DenunciaRepository;
import com.aqConnecta.repository.ProjetoRepository;
import com.aqConnecta.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final EmailService emailService;

    @Value("${projeto.denuncia.limiar:5}")
    private int limiar;

    @Value("${cors.urls:http://localhost:3000}")
    private String corsUrls;

    public ResponseEntity<Object> criar(UUID idProjeto, DenunciaRequest req, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Projeto> projetoOpt = projetoRepository.findById(idProjeto);
            if (projetoOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Projeto não encontrado.", HttpStatus.NOT_FOUND);
            }
            Projeto projeto = projetoOpt.get();
            if (denunciaRepository.existsByProjetoIdAndDenuncianteId(idProjeto, usuario.getId())) {
                return ResponseHandler.generateResponse("Você já denunciou este projeto.", HttpStatus.CONFLICT);
            }
            if (req.getMotivo() == MotivoDenuncia.OUTRO && (req.getDescricao() == null || req.getDescricao().isBlank())) {
                return ResponseHandler.generateResponse("Descreva o motivo da denúncia.", HttpStatus.BAD_REQUEST);
            }
            Denuncia denuncia = Denuncia.builder()
                    .projeto(projeto)
                    .denunciante(usuario)
                    .motivo(req.getMotivo())
                    .descricao(req.getDescricao())
                    .status(StatusDenuncia.PENDENTE)
                    .criadoEm(LocalDateTime.now())
                    .build();
            denunciaRepository.saveAndFlush(denuncia);

            long total = denunciaRepository.countByProjetoId(idProjeto);
            if (total >= limiar) {
                notificarAdmins(projeto, total);
            }
            return ResponseHandler.generateResponse("Denúncia registrada. Obrigado por reportar.", HttpStatus.CREATED);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao registrar denúncia", e);
            return ResponseHandler.generateResponse("Houve um erro ao registrar a denúncia.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> listar(StatusDenuncia status, Pageable pageable) {
        try {
            Page<Denuncia> page = status != null
                    ? denunciaRepository.findByStatusOrderByCriadoEmDesc(status, pageable)
                    : denunciaRepository.findAllByOrderByCriadoEmDesc(pageable);
            List<DenunciaResponse> resposta = page.getContent().stream().map(d -> {
                DenunciaResponse r = new DenunciaResponse();
                r.inToOut(d);
                return r;
            }).collect(Collectors.toList());
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, resposta);
        } catch (Exception e) {
            log.error("Erro ao listar denúncias", e);
            return ResponseHandler.generateResponse("Houve um erro ao listar as denúncias.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> alterarStatus(UUID idDenuncia, AlterarStatusDenunciaRequest req, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Denuncia> denunciaOpt = denunciaRepository.findById(idDenuncia);
            if (denunciaOpt.isEmpty()) {
                return ResponseHandler.generateResponse("Denúncia não encontrada.", HttpStatus.NOT_FOUND);
            }
            Denuncia denuncia = denunciaOpt.get();
            denuncia.setStatus(req.getStatus());
            if (req.getStatus() != StatusDenuncia.PENDENTE) {
                denuncia.setResolvidoEm(LocalDateTime.now());
                denuncia.setResolvidoPor(usuario);
            } else {
                denuncia.setResolvidoEm(null);
                denuncia.setResolvidoPor(null);
            }
            denunciaRepository.save(denuncia);
            DenunciaResponse r = new DenunciaResponse();
            r.inToOut(denuncia);
            return ResponseHandler.generateResponse("Status atualizado com sucesso!", HttpStatus.OK, r);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Erro ao alterar status da denúncia", e);
            return ResponseHandler.generateResponse("Houve um erro ao alterar o status.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void notificarAdmins(Projeto projeto, long total) {
        try {
            List<Usuario> admins = usuarioRepository.findByPermissaoDescricao(Permissao.ROLE_ADMIN);
            String base = corsUrls == null || corsUrls.isBlank() ? "" : corsUrls.split(",")[0].trim();
            String link = base + "/admin/denuncias";
            String mensagem = "[PRIORIDADE ALTA] O projeto \"" + projeto.getTitulo() + "\" atingiu " + total
                    + " denúncias e precisa de revisão.";
            for (Usuario admin : admins) {
                emailService.sendEmail(admin.getEmail(), "[Prioridade alta] Projeto com muitas denúncias - AQConnecta",
                        emailService.criarCorpoEmail(admin.getNome(), mensagem, link));
            }
        } catch (Exception e) {
            log.warn("Falha ao notificar administradores sobre denúncias: {}", e.getMessage());
        }
    }
}
