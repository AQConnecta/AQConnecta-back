package com.aqConnecta.repository;

import com.aqConnecta.model.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PermissaoRepository extends JpaRepository<Permissao, Long> {
}
