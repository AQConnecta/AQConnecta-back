package com.aqConnecta.repository;

import com.aqConnecta.model.ProjetoMembro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjetoMembroRepository extends JpaRepository<ProjetoMembro, UUID> {

    Optional<ProjetoMembro> findByProjetoIdAndUsuarioIdAndAtivoTrue(UUID projetoId, UUID usuarioId);

    Optional<ProjetoMembro> findByProjetoIdAndUsuarioId(UUID projetoId, UUID usuarioId);

    List<ProjetoMembro> findByProjetoIdAndAtivoTrue(UUID projetoId);

    List<ProjetoMembro> findByProjetoId(UUID projetoId);

    Page<ProjetoMembro> findByProjetoIdOrderByAtivoDescDataEntradaAsc(UUID projetoId, Pageable pageable);
}
