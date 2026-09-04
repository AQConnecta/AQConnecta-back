package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.Comentario;
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
public class ComentarioResponse {
    private UUID id;
    private String corpo;
    private PublicadorResponse autor;
    private boolean podeDeletar;
    private LocalDateTime criadoEm;

    @Builder.Default
    private List<ComentarioResponse> respostas = new ArrayList<>();

    public void inToOut(Comentario comentario) {
        this.id = comentario.getId();
        this.corpo = comentario.getCorpo();
        this.criadoEm = comentario.getCriadoEm();
        if (comentario.getAutor() != null) {
            PublicadorResponse autorResponse = new PublicadorResponse();
            autorResponse.inToOut(comentario.getAutor());
            this.autor = autorResponse;
        }
    }
}
