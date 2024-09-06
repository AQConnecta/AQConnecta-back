package com.aqConnecta.repository;

import com.aqConnecta.DTOs.response.CompetenciaCountDTO;
import com.aqConnecta.model.Competencia;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface CompetenciaRepository extends JpaRepository<Competencia, UUID> {

    @Query(value = "select TC.ID as id, TC.DESCRICAO as descricao, COUNT(TV.ID) as count " +
            "from RL_VAGA_COMPETENCIA " +
            "INNER JOIN TB_COMPETENCIA TC on RL_VAGA_COMPETENCIA.ID_COMPETENCIA = TC.ID " +
            "INNER JOIN TB_VAGA TV ON RL_VAGA_COMPETENCIA.ID_VAGA = TV.ID " +
            "GROUP BY TC.ID, TC.DESCRICAO", nativeQuery = true)
    List<Object[]> countCompetenciasInVagas();

    Page<Competencia> findByDescricaoContainingIgnoreCase(String descricao, Pageable pageable);

    List<Competencia> findByDescricaoIgnoreCase(String descricao);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM RL_USUARIO_COMPETENCIA WHERE ID_COMPETENCIA = :competenciaId", nativeQuery = true)
    void deleteFromUsuarioCompetencia(@Param("competenciaId") UUID competenciaId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM RL_VAGA_COMPETENCIA WHERE ID_COMPETENCIA = :competenciaId", nativeQuery = true)
    void deleteFromVagaCompetencia(@Param("competenciaId") UUID competenciaId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM TB_COMPETENCIA WHERE ID = :competenciaId", nativeQuery = true)
    void deleteCompetencia(@Param("competenciaId") UUID competenciaId);
}
