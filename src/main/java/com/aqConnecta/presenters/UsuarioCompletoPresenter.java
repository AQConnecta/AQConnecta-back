package com.aqConnecta.presenters;

import com.aqConnecta.model.Usuario;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioCompletoPresenter(
    @NotNull String id,
    @NotNull String email,
    @NotNull String nome,
    String descricao,
    @NotNull String userUrl,
    @NotNull List<PermissaoPresenter> permissao,
    @NotNull List<CompetenciaPresenter> competencias,
    @NotNull List<EnderecoPresenter> enderecos,
    @NotNull List<ExperienciaPresenter> experiencias,
    @NotNull List<FormacaoAcademicaPresenter> formacoesAcademicas,
    @NotNull Boolean deletado,
    @NotNull Boolean ativado,
    String fotoPerfil,
    @NotNull List<CurriculoPresenter> curriculos,
    String telefone,
    String curriculoLattesUrl,
    String githubProfileUrl,
    String linkedinProfileUrl
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
            usuario.getCurriculo().stream().map(CurriculoPresenter::apresentar).toList(),
            usuario.getTelefone(),
            usuario.getCurriculoLattes() == null ? null : usuario.getCurriculoLattes().toString(),
            usuario.getGithubProfile() == null ? null : usuario.getGithubProfile().toString(),
            usuario.getLinkedinProfile() == null ? null : usuario.getLinkedinProfile().toString()
        );
    }
}
