package com.aqConnecta.repository;

import com.aqConnecta.model.Universidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UniversidadeRepository extends JpaRepository<Universidade, UUID> {
    Page<Universidade> findByNomeInstituicaoContainingIgnoreCaseOrSiglaContainingIgnoreCase(String nome, String sigla, Pageable pageable);
}
