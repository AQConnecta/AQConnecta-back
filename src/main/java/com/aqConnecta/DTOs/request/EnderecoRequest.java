package com.aqConnecta.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnderecoRequest {

    @NotBlank(message = "CEP é obrigatório")
    @Size(max = 10, message = "CEP deve ter no máximo 10 caracteres")
    private String cep;

    @NotBlank(message = "Rua é obrigatória")
    private String rua;

    @NotBlank(message = "Bairro é obrigatório")
    private String bairro;

    @NotBlank(message = "Cidade é obrigatória")
    private String cidade;

    @NotBlank(message = "Estado é obrigatório")
    private String estado;

    @NotBlank(message = "País é obrigatório")
    private String pais;

    @NotBlank(message = "Número da casa é obrigatório")
    private String numeroCasa;

    private String complemento;
}
