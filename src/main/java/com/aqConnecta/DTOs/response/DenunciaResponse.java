package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.Denuncia;
import com.aqConnecta.model.enums.MotivoDenuncia;
import com.aqConnecta.model.enums.StatusDenuncia;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DenunciaResponse {
    private UUID id;
    private UUID projetoId;
    private String projetoTitulo;
    private PublicadorResponse denunciante;
    private MotivoDenuncia motivo;
    private String descricao;
    private StatusDenuncia status;
    private String resolvidoPorNome;
    private LocalDateTime criadoEm;
    private LocalDateTime resolvidoEm;

    public void inToOut(Denuncia denuncia) {
        this.id = denuncia.getId();
        if (denuncia.getProjeto() != null) {
            this.projetoId = denuncia.getProjeto().getId();
            this.projetoTitulo = denuncia.getProjeto().getTitulo();
        }
        if (denuncia.getDenunciante() != null) {
            PublicadorResponse denuncianteResponse = new PublicadorResponse();
            denuncianteResponse.inToOut(denuncia.getDenunciante());
            this.denunciante = denuncianteResponse;
        }
        this.motivo = denuncia.getMotivo();
        this.descricao = denuncia.getDescricao();
        this.status = denuncia.getStatus();
        if (denuncia.getResolvidoPor() != null) {
            this.resolvidoPorNome = denuncia.getResolvidoPor().getNome();
        }
        this.criadoEm = denuncia.getCriadoEm();
        this.resolvidoEm = denuncia.getResolvidoEm();
    }
}
