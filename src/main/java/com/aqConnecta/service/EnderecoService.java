package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.EnderecoRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.model.Endereco;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.repository.EnderecoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class EnderecoService {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private EnderecoRepository enderecoRepository;

    public ResponseEntity<Object> cadastrarEndereco(EnderecoRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Endereco endereco = Endereco.builder()
                    .usuario(usuario)
                    .cep(registro.getCep())
                    .rua(registro.getRua())
                    .bairro(registro.getBairro())
                    .cidade(registro.getCidade())
                    .estado(registro.getEstado())
                    .pais(registro.getPais())
                    .numeroCasa(registro.getNumeroCasa())
                    .complemento(registro.getComplemento())
                    .build();
            enderecoRepository.save(endereco);
            return ResponseHandler.generateResponse("Endereco cadastrado com sucesso!", HttpStatus.CREATED, endereco);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> listarEnderecosPorUsuario(UUID idUsuario) {
        try {
            Usuario usuario = usuarioService.localizar(idUsuario);
            Set<Endereco> enderecos = enderecoRepository.findByUsuario(usuario);
            if (!enderecos.isEmpty()) {
                return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, enderecos);
            }
            return ResponseHandler.generateResponse("Nenhum endereço encontrado para este usuário.", HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar listar os endereços do usuário.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> localizarEndereco(UUID idEndereco) {
        try {
            Optional<Endereco> endereco = enderecoRepository.findById(idEndereco);
            if (endereco.isPresent()) {
                return ResponseHandler.generateResponse("Localizado com sucesso", HttpStatus.OK, endereco);
            }
            return ResponseHandler.generateResponse("Nenhum endereço encontrado para este ID.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar localizar o endereço.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> alterarEndereco(UUID idEndereco, EnderecoRequest registro, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Endereco> endereco = enderecoRepository.findById(idEndereco);
            if (endereco.isPresent()) {
                if (!endereco.get().getUsuario().getId().equals(usuario.getId())) {
                    return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
                }
                Endereco enderecoAlterado = Endereco.builder()
                        .id(idEndereco)
                        .usuario(endereco.get().getUsuario())
                        .cep(registro.getCep())
                        .rua(registro.getRua())
                        .bairro(registro.getBairro())
                        .cidade(registro.getCidade())
                        .estado(registro.getEstado())
                        .pais(registro.getPais())
                        .numeroCasa(registro.getNumeroCasa())
                        .complemento(registro.getComplemento())
                        .build();
                enderecoRepository.save(enderecoAlterado);
                return ResponseHandler.generateResponse("Endereço atualizado com sucesso!", HttpStatus.OK, enderecoAlterado);
            }
            return ResponseHandler.generateResponse("Erro ao encontrar o endereço!", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> deletarEndereco(UUID idEndereco, String emailAutenticado) {
        try {
            Usuario usuario = usuarioService.localizarPorEmail(emailAutenticado);
            Optional<Endereco> endereco = enderecoRepository.findById(idEndereco);
            if (endereco.isPresent()) {
                if (!endereco.get().getUsuario().getId().equals(usuario.getId())) {
                    return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
                }
                enderecoRepository.deleteById(idEndereco);
                return ResponseHandler.generateResponse("Deletado com sucesso", HttpStatus.OK);
            }
            return ResponseHandler.generateResponse("Não é possível excluir um endereço não existente.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar excluir o endereço.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
