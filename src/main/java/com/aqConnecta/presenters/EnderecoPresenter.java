package com.aqConnecta.presenters;

import com.aqConnecta.model.Endereco;
import jakarta.validation.constraints.NotNull;

public record EnderecoPresenter(
    @NotNull String id,
    @NotNull String cep,
    @NotNull String rua,
    @NotNull String bairro,
    @NotNull String cidade,
    @NotNull String estado,
    @NotNull String pais,
    @NotNull String numeroCasa,
    @NotNull String complemento
) {
    public static EnderecoPresenter apresentar(Endereco endereco) {
        return new EnderecoPresenter(
            endereco.getId().toString(),
            endereco.getCep(),
            endereco.getRua(),
            endereco.getBairro(),
            endereco.getCidade(),
            endereco.getEstado(),
            endereco.getPais(),
            endereco.getNumeroCasa(),
            endereco.getComplemento()
        );
    }
}
