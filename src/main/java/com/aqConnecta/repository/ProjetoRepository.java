package com.aqConnecta.repository;

import com.aqConnecta.model.Projeto;
import com.aqConnecta.model.enums.StatusProjeto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, UUID> {

    @Query("SELECT p FROM Projeto p WHERE p.visibilidade = com.aqConnecta.model.enums.VisibilidadeProjeto.PUBLICO " +
            "AND (:titulo = '' OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) " +
            "AND (:idArea IS NULL OR p.area.id = :idArea) " +
            "AND (:status IS NULL OR p.status = :status)")
    Page<Projeto> listarPublicos(@Param("titulo") String titulo,
                                 @Param("idArea") UUID idArea,
                                 @Param("status") StatusProjeto status,
                                 Pageable pageable);

    @Query("SELECT p FROM Projeto p WHERE p.dono.id = :usuarioId " +
            "OR p.id IN (SELECT m.projeto.id FROM ProjetoMembro m WHERE m.usuario.id = :usuarioId AND m.ativo = true)")
    List<Projeto> findMeusProjetos(@Param("usuarioId") UUID usuarioId);

    long countByAreaId(UUID areaId);
}
