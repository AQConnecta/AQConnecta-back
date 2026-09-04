package com.aqConnecta.repository;

import com.aqConnecta.model.PostagemImagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostagemImagemRepository extends JpaRepository<PostagemImagem, UUID> {

    List<PostagemImagem> findByPostagemIdOrderByOrdemAsc(UUID postagemId);

    long countByPostagemId(UUID postagemId);
}
