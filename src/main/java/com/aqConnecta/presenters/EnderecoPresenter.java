package com.aqConnecta.presenters;

import com.aqConnecta.model.Endereco;

public record EnderecoPresenter(
    String id,
    String cep,
    String rua,
    String bairro,
    String cidade,
    String estado,
    String pais,
    String numeroCasa,
    String complemento
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
