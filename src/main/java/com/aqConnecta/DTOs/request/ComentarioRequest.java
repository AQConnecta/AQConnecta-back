package com.aqConnecta.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComentarioRequest {

    @NotBlank(message = "Comentário é obrigatório")
    @Size(max = 2000, message = "Comentário deve ter no máximo 2000 caracteres")
    private String corpo;

    private UUID idComentarioPai;
}
