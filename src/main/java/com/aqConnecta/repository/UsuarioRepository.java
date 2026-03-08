package com.aqConnecta.repository;

import com.aqConnecta.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    @EntityGraph(attributePaths = {
        "permissao",
        "competencias",
        "enderecos",
        "experiencias",
        "formacoesAcademicas",
        "curriculo"
    })
    Optional<Usuario> findByUserUrl(String userUrl);

    List<Usuario> findAllByNomeContainingIgnoreCase(String nome);

    boolean existsByUserUrl(String userUrl);

    Usuario findByEmailIgnoreCase(String email);

    Boolean existsByEmail(String email);

}
