package com.aqConnecta.repository;

import com.aqConnecta.model.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, UUID> {

    Page<Comentario> findByPostagemIdAndComentarioPaiIsNullOrderByCriadoEmDesc(UUID postagemId, Pageable pageable);

    List<Comentario> findByComentarioPaiIdInOrderByCriadoEmAsc(List<UUID> comentarioPaiIds);

    List<Comentario> findByComentarioPaiId(UUID comentarioPaiId);

    long countByPostagemId(UUID postagemId);
}
