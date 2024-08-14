package com.aqConnecta.repository;

import com.aqConnecta.model.Candidatura;
import com.aqConnecta.model.ConfirmaToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidaturaRepository extends JpaRepository<Candidatura, Integer>{
    List<Candidatura> findAllCandidaturaByVagaId(UUID vagaId);
}
