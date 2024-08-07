package com.aqConnecta.repository;

import com.aqConnecta.model.Experiencia;
import com.aqConnecta.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ExperienciaRepository extends JpaRepository<Experiencia, UUID> {

    Set<Experiencia> findByUsuario(Usuario usuario);
}
