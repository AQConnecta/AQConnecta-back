package com.aqConnecta.presenters;

import com.aqConnecta.model.Usuario;
import jakarta.annotation.Nullable;

import java.util.List;

public record UsuarioCompletoPresenter(
    String id,
    String email,
    String nome,
    @Nullable String descricao,
    String userUrl,
    List<PermissaoPresenter> permissao,
    List<CompetenciaPresenter> competencias,
    List<EnderecoPresenter> enderecos,
    List<ExperienciaPresenter> experiencias,
    List<FormacaoAcademicaPresenter> formacoesAcademicas,
    Boolean deletado,
    Boolean ativado,
    String fotoPerfil,
    List<CurriculoPresenter> curriculos
) {
    public static UsuarioCompletoPresenter apresentar(Usuario usuario) {
        return new UsuarioCompletoPresenter(
            usuario.getId().toString(),
            usuario.getEmail(),
            usuario.getNome(),
            usuario.getDescricao(),
            usuario.getUserUrl(),
            usuario.getPermissao().stream().map(PermissaoPresenter::apresentar).toList(),
            usuario.getCompetencias().stream().map(CompetenciaPresenter::apresentar).toList(),
            usuario.getEnderecos().stream().map(EnderecoPresenter::apresentar).toList(),
            usuario.getExperiencias().stream().map(ExperienciaPresenter::apresentar).toList(),
            usuario.getFormacoesAcademicas().stream().map(FormacaoAcademicaPresenter::apresentar).toList(),
            usuario.getDeletado(),
            usuario.getAtivado(),
            usuario.getFotoPerfil(),
            usuario.getCurriculo().stream().map(CurriculoPresenter::apresentar).toList()
        );
    }
}
