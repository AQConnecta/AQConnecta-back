package com.aqConnecta.DTOs.request;

import com.aqConnecta.model.Permissao;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistroRequest {

    private String nome;

    private String email;

    private String senha;

}
