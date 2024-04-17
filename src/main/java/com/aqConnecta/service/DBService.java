package com.aqConnecta.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.Permissao;
import com.aqConnecta.repository.UsuarioRepository;


@Service
public class DBService {
	
	@Autowired
	private UsuarioRepository repository;
	@Autowired
	private PasswordEncoder encoder;
	
	
	public void instanciaDB() {
		Usuario usuario = Usuario.builder()
				.id(UUID.randomUUID())
				.deletado(false)
				.email("riume@teste2.com")
				.nome("riume2")
				.senha(encoder.encode("teste123"))
				.build();

		repository.save(usuario);
	}
}
