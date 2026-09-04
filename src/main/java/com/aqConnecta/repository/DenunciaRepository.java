package com.aqConnecta.repository;

import com.aqConnecta.model.Denuncia;
import com.aqConnecta.model.enums.StatusDenuncia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, UUID> {

    boolean existsByProjetoIdAndDenuncianteId(UUID projetoId, UUID denuncianteId);

    long countByProjetoId(UUID projetoId);

    Page<Denuncia> findAllByOrderByCriadoEmDesc(Pageable pageable);

    Page<Denuncia> findByStatusOrderByCriadoEmDesc(StatusDenuncia status, Pageable pageable);
}
