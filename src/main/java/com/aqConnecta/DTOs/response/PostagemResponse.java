package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.Postagem;
import com.aqConnecta.model.enums.StatusPostagem;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostagemResponse {
    private UUID id;
    private UUID projetoId;
    private String titulo;
    private String corpo;
    private StatusPostagem status;
    private PublicadorResponse autor;

    @Builder.Default
    private List<ProjetoImagemResponse> imagens = new ArrayList<>();

    private long totalComentarios;
    private boolean podeEditar;
    private LocalDateTime publicadoEm;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public void inToOut(Postagem postagem) {
        this.id = postagem.getId();
        this.projetoId = postagem.getProjeto() != null ? postagem.getProjeto().getId() : null;
        this.titulo = postagem.getTitulo();
        this.corpo = postagem.getCorpo();
        this.status = postagem.getStatus();
        if (postagem.getAutor() != null) {
            PublicadorResponse autorResponse = new PublicadorResponse();
            autorResponse.inToOut(postagem.getAutor());
            this.autor = autorResponse;
        }
        this.publicadoEm = postagem.getPublicadoEm();
        this.criadoEm = postagem.getCriadoEm();
        this.atualizadoEm = postagem.getAtualizadoEm();
    }
}
