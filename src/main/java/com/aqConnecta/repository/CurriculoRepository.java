package com.aqConnecta.repository;

import com.aqConnecta.model.ConfirmaToken;
import com.aqConnecta.model.Curriculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CurriculoRepository extends JpaRepository<Curriculo, Integer> {
}
