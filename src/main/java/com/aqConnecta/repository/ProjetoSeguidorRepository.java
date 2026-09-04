package com.aqConnecta.repository;

import com.aqConnecta.model.ProjetoSeguidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjetoSeguidorRepository extends JpaRepository<ProjetoSeguidor, UUID> {

    Optional<ProjetoSeguidor> findByProjetoIdAndUsuarioId(UUID projetoId, UUID usuarioId);

    boolean existsByProjetoIdAndUsuarioId(UUID projetoId, UUID usuarioId);

    long countByProjetoId(UUID projetoId);

    List<ProjetoSeguidor> findByUsuarioIdOrderByCriadoEmDesc(UUID usuarioId);
}
