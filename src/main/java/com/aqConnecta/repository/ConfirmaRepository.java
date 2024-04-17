package com.aqConnecta.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aqConnecta.model.ConfirmaToken;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfirmaRepository extends JpaRepository<ConfirmaToken, UUID> {
	
	Optional<ConfirmaToken> findByToken(String confirmaToken);
		
}
