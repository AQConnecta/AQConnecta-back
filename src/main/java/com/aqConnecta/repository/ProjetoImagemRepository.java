package com.aqConnecta.repository;

import com.aqConnecta.model.ProjetoImagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjetoImagemRepository extends JpaRepository<ProjetoImagem, UUID> {

    List<ProjetoImagem> findByProjetoIdOrderByOrdemAsc(UUID projetoId);

    long countByProjetoId(UUID projetoId);
}
