package com.aqConnecta.repository;

import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface VagaRepository extends JpaRepository<Vaga, UUID> {

    Set<Vaga> findByPublicador(Usuario usuario);

    List<Vaga> findByTituloContainingIgnoreCase(String titulo);

    @Query("SELECT v FROM Vaga v JOIN v.competencias c WHERE c.id = :competenciaId")
    List<Vaga> findByCompetenciaId(@Param("competenciaId") UUID competenciaId);
}
