package com.aqConnecta.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.aqConnecta.model.Usuario;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByUserUrl(String userUrl);

    List<Usuario> findAllByNomeContainingIgnoreCase(String nome);

    Page<Usuario> findAllByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Boolean existsByUserUrl(String userUrl);

    Usuario findByEmailIgnoreCase(String email);

    Boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u JOIN u.permissao p WHERE p.descricao = :descricao")
    List<Usuario> findByPermissaoDescricao(@Param("descricao") String descricao);
}
