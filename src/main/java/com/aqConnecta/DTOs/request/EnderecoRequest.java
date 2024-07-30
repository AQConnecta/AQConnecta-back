package com.aqConnecta.DTOs.request;

import lombok.*;
import org.apache.logging.log4j.util.Strings;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnderecoRequest {
    private String cep;
    private String rua;
    private String bairro;
    private String cidade;
    private String estado;
    private String pais;
    private String numeroCasa;
    private String complemento;

    public boolean validarDadosObrigatorios() {
        return !Strings.isEmpty(cep) && !Strings.isEmpty(rua) && !Strings.isEmpty(bairro) &&
                !Strings.isEmpty(cidade) && !Strings.isEmpty(estado) && !Strings.isEmpty(pais) &&
                !Strings.isEmpty(numeroCasa) && !Strings.isEmpty(complemento);
    }
}
