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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public ResponseEntity<Object> cadastrarEndereco(EnderecoRequest registro) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        if (!registro.validarDadosObrigatorios()) {
            return ResponseHandler.generateResponse("Error: Campos obrigatorios não informados", HttpStatus.BAD_REQUEST);
        }
        try {
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            Endereco endereco = new Endereco().builder()
                    .id(UUID.randomUUID())
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
            return ResponseHandler.generateResponse("Endereco cadastrado com súcesso!", HttpStatus.CREATED, endereco);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Object> listarEnderecosPorUsuario(UUID idUsuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO remover essa bosta de contains dps do riume arrumar o security
        if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
            return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
        }
        try {
            Optional<Endereco> experiencia = enderecoRepository.findById(idEndereco);
            if (experiencia.isPresent()) {
                return ResponseHandler.generateResponse("Localizado com sucesso", HttpStatus.OK, experiencia);
            }
            return ResponseHandler.generateResponse("Nenhum endereço encontrada para este ID.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar localizar o endereço.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    public ResponseEntity<Object> alterarEndereco(UUID idEndereco, EnderecoRequest registro) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // TODO remover essa bosta de contains dps do riume arrumar o security
            if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
            }
            if (!registro.validarDadosObrigatorios() && idEndereco != null && !idEndereco.toString().isEmpty()) {
                return ResponseHandler.generateResponse("Error: Campos obrigatorios não informados", HttpStatus.BAD_REQUEST);
            }
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            Optional<Endereco> endereco = enderecoRepository.findById(idEndereco);
            if (endereco.isPresent()) {
                if (!endereco.get().getUsuario().getId().equals(usuario.getId())) {
                    return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.", HttpStatus.UNAUTHORIZED);
                }
                Endereco enderecoAlterado = new Endereco()
                        .builder()
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
                return ResponseHandler.generateResponse("Endereço atualizado com súcesso!", HttpStatus.CREATED, endereco);
            }
            return ResponseHandler.generateResponse("Erro ao encontrar o endereço!", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<Object> deletarEndereco(UUID idEndereco) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // TODO remover essa bosta de contains dps do riume arrumar o security
            if (authentication != null && authentication.isAuthenticated() && authentication.getName().toLowerCase().contains("anonymous")) {
                return ResponseHandler.generateResponse("Precisa estar logado para continuar.", HttpStatus.UNAUTHORIZED);
            }
            Usuario usuario = usuarioService.localizarPorEmail(authentication.getName());
            Optional<Endereco> endereco = enderecoRepository.findById(idEndereco);
            if (endereco.isPresent()) {
                if (!endereco.get().getUsuario().getId().equals(usuario.getId())) {
                    return ResponseHandler.generateResponse("Error: Você não tem permissão para alterar esse registro.", HttpStatus.FORBIDDEN);
                }
                enderecoRepository.deleteById(idEndereco);
            } else {
                return ResponseHandler.generateResponse("Não é possível excluir uma experiencia não existente.", HttpStatus.NOT_FOUND);
            }
            return ResponseHandler.generateResponse("Deletado com sucesso", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao tentar excluir o endereço.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
