package com.aqConnecta.repository;

import com.aqConnecta.model.Competencia;
import com.aqConnecta.model.Endereco;
import com.aqConnecta.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CompetenciaRepository extends JpaRepository<Competencia, UUID> {

}
