package com.aqConnecta.DTOs.request;

import lombok.*;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioRequest {
    private String descricao;
}
