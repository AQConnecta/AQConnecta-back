package com.aqConnecta.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.aqConnecta.model.Usuario;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

	Optional<Usuario> findByEmail(String email);

	Usuario findByEmailIgnoreCase(String email);

	Boolean existsByEmail(String email);

}
