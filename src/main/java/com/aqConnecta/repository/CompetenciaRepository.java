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

import java.time.LocalDateTime;
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

    /**
     * Query otimizada para cálculo de hot competencies com dados temporais
     * Retorna: ID, DESCRICAO, TOTAL_COUNT, RECENT_COUNT, LATEST_VAGA_DATE
     */
    @Query(value = """
            SELECT 
                TC.ID as competencia_id,
                TC.DESCRICAO as descricao,
                COUNT(TV.ID) as total_count,
                COUNT(CASE WHEN TV.CRIADO_EM >= :cutoffDate THEN TV.ID END) as recent_count,
                MAX(TV.CRIADO_EM) as latest_vaga_date
            FROM RL_VAGA_COMPETENCIA RVC
            INNER JOIN TB_COMPETENCIA TC ON RVC.ID_COMPETENCIA = TC.ID
            INNER JOIN TB_VAGA TV ON RVC.ID_VAGA = TV.ID
            WHERE TV.DELETADO_EM IS NULL
            GROUP BY TC.ID, TC.DESCRICAO
            HAVING COUNT(TV.ID) > 0
            ORDER BY recent_count DESC, total_count DESC
            LIMIT 1000
            """, nativeQuery = true)
    List<Object[]> getHotCompetenciesData(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Query para obter estatísticas de competências por período
     */
    @Query(value = """
            SELECT 
                DATE_FORMAT(TV.CRIADO_EM, '%Y-%m') as month,
                TC.ID as competencia_id,
                TC.DESCRICAO as descricao,
                COUNT(TV.ID) as count
            FROM RL_VAGA_COMPETENCIA RVC
            INNER JOIN TB_COMPETENCIA TC ON RVC.ID_COMPETENCIA = TC.ID
            INNER JOIN TB_VAGA TV ON RVC.ID_VAGA = TV.ID
            WHERE TV.DELETADO_EM IS NULL
            AND TV.CRIADO_EM >= :startDate
            GROUP BY DATE_FORMAT(TV.CRIADO_EM, '%Y-%m'), TC.ID, TC.DESCRICAO
            ORDER BY month DESC, count DESC
            """, nativeQuery = true)
    List<Object[]> getCompetencyTrends(@Param("startDate") LocalDateTime startDate);

    /**
     * Query para competências emergentes (crescimento rápido)
     */
    @Query(value = """
            SELECT 
                TC.ID as competencia_id,
                TC.DESCRICAO as descricao,
                COUNT(CASE WHEN TV.CRIADO_EM >= :recentDate THEN TV.ID END) as recent_count,
                COUNT(CASE WHEN TV.CRIADO_EM < :recentDate AND TV.CRIADO_EM >= :oldDate THEN TV.ID END) as old_count,
                MAX(TV.CRIADO_EM) as latest_date
            FROM RL_VAGA_COMPETENCIA RVC
            INNER JOIN TB_COMPETENCIA TC ON RVC.ID_COMPETENCIA = TC.ID
            INNER JOIN TB_VAGA TV ON RVC.ID_VAGA = TV.ID
            WHERE TV.DELETADO_EM IS NULL
            GROUP BY TC.ID, TC.DESCRICAO
            HAVING recent_count > 0
            ORDER BY (recent_count - COALESCE(old_count, 0)) DESC
            LIMIT 50
            """, nativeQuery = true)
    List<Object[]> getEmergingCompetencies(@Param("recentDate") LocalDateTime recentDate, 
                                         @Param("oldDate") LocalDateTime oldDate);

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
