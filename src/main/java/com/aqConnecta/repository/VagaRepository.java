package com.aqConnecta.repository;

import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface VagaRepository extends JpaRepository<Vaga, UUID> {

    Set<Vaga> findByPublicador(Usuario usuario);

}
