package com.aqConnecta.repository;

import com.aqConnecta.model.FormacaoAcademica;
import com.aqConnecta.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface FormacaoAcademicaRepository extends JpaRepository<FormacaoAcademica, UUID> {

    Set<FormacaoAcademica> findByUsuario(Usuario usuario);
}
