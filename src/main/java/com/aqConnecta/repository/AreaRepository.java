package com.aqConnecta.repository;

import com.aqConnecta.model.Area;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AreaRepository extends JpaRepository<Area, UUID> {

    List<Area> findByDescricaoIgnoreCase(String descricao);

    Page<Area> findByDescricaoContainingIgnoreCase(String descricao, Pageable pageable);
}
