package com.aqConnecta.service;

import com.aqConnecta.model.Projeto;
import com.aqConnecta.model.ProjetoMembro;
import com.aqConnecta.model.Usuario;
import com.aqConnecta.model.enums.PapelProjeto;
import com.aqConnecta.model.enums.VisibilidadeProjeto;
import com.aqConnecta.repository.ProjetoMembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProjetoAutorizacaoService {

    private final ProjetoMembroRepository projetoMembroRepository;

    public PapelProjeto papelDoUsuario(Projeto projeto, Usuario usuario) {
        if (projeto == null || usuario == null) {
            return null;
        }
        if (projeto.getDono() != null && projeto.getDono().getId().equals(usuario.getId())) {
            return PapelProjeto.DONO;
        }
        Optional<ProjetoMembro> membro = projetoMembroRepository
                .findByProjetoIdAndUsuarioIdAndAtivoTrue(projeto.getId(), usuario.getId());
        return membro.map(ProjetoMembro::getPapel).orElse(null);
    }

    public boolean ehAdmin(Usuario usuario) {
        return usuario != null && !usuario.verificarUsuarioNaoEAdministrador();
    }

    public boolean podeGerenciar(Projeto projeto, Usuario usuario) {
        if (ehAdmin(usuario)) {
            return true;
        }
        PapelProjeto papel = papelDoUsuario(projeto, usuario);
        return papel == PapelProjeto.DONO || papel == PapelProjeto.EDITOR;
    }

    public boolean ehDono(Projeto projeto, Usuario usuario) {
        return ehAdmin(usuario) || papelDoUsuario(projeto, usuario) == PapelProjeto.DONO;
    }

    public boolean podeVisualizar(Projeto projeto, Usuario usuario) {
        if (projeto == null) {
            return false;
        }
        if (projeto.getVisibilidade() == VisibilidadeProjeto.PUBLICO) {
            return true;
        }
        if (ehAdmin(usuario)) {
            return true;
        }
        return papelDoUsuario(projeto, usuario) != null;
    }
}
