package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.Postagem;
import com.aqConnecta.model.enums.StatusPostagem;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostagemResumoResponse {
    private UUID id;
    private UUID projetoId;
    private String titulo;
    private StatusPostagem status;
    private PublicadorResponse autor;
    private String capaUrl;
    private long totalComentarios;
    private LocalDateTime publicadoEm;
    private LocalDateTime criadoEm;

    public void inToOut(Postagem postagem) {
        this.id = postagem.getId();
        this.projetoId = postagem.getProjeto() != null ? postagem.getProjeto().getId() : null;
        this.titulo = postagem.getTitulo();
        this.status = postagem.getStatus();
        if (postagem.getAutor() != null) {
            PublicadorResponse autorResponse = new PublicadorResponse();
            autorResponse.inToOut(postagem.getAutor());
            this.autor = autorResponse;
        }
        this.publicadoEm = postagem.getPublicadoEm();
        this.criadoEm = postagem.getCriadoEm();
    }
}
