package com.aqConnecta.repository;

import com.aqConnecta.model.Postagem;
import com.aqConnecta.model.enums.StatusPostagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostagemRepository extends JpaRepository<Postagem, UUID> {

    Page<Postagem> findByProjetoIdAndStatusOrderByPublicadoEmDesc(UUID projetoId, StatusPostagem status, Pageable pageable);

    Page<Postagem> findByProjetoIdOrderByCriadoEmDesc(UUID projetoId, Pageable pageable);
}
