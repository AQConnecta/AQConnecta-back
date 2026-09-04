package com.aqConnecta.repository;

import com.aqConnecta.model.ProjetoConvite;
import com.aqConnecta.model.enums.StatusConvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjetoConviteRepository extends JpaRepository<ProjetoConvite, UUID> {

    Optional<ProjetoConvite> findByProjetoIdAndUsuarioConvidadoIdAndStatus(UUID projetoId, UUID usuarioId, StatusConvite status);

    List<ProjetoConvite> findByProjetoIdAndStatus(UUID projetoId, StatusConvite status);

    List<ProjetoConvite> findByUsuarioConvidadoIdAndStatus(UUID usuarioId, StatusConvite status);
}
