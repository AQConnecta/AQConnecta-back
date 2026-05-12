package com.aqConnecta.DTOs.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EditarUsuarioRequest {

    // Sintaxe bizarra pra validar o conteúdo interno do `JsonNullable`, mas funciona...

    private JsonNullable<
        // Não usamos o @NotBlank nem o @NotEmpty porque _pode_ ser `null`, é esperado que seja.
        @Size(min = 1, message = "Sua descrição não pode ser vazia.")
        @Size(max = 1000, message = "A descrição não pode ultrapassar {max} caracteres.")
            String>
        descricao = JsonNullable.undefined();

    private JsonNullable<
        @Size(min = 1, message = "Seu número de telefone não deve estar vazio.")
        @Size(max = 20, message = "O número de telefone não deve ultrapassar {max} dígitos.")
            String>
        telefone = JsonNullable.undefined();

    private JsonNullable<
        @URL(message = "Você deve fornecer um URL válido para o seu Currículo Lattes.")
        @Size(max = 255, message = "O URL do seu perfil no Currículo Lattes não pode ultrapassar {max} caracteres.")
            String>
        curriculoLattes = JsonNullable.undefined();

    private JsonNullable<
        @Size(max = 255, message = "O URL do seu perfil do GitHub não pode ultrapassar {max} caracteres.")
        @URL(message = "Você deve fornecer um URL válido para o seu perfil no GitHub.")
            String>
        perfilGitHub = JsonNullable.undefined();

    private JsonNullable<
        @Size(max = 255, message = "O URL do seu perfil no Linkedin não pode ultrapassar {max} caracteres.")
        @URL(message = "Você deve fornecer um URL válido para o seu perfil no Linkedin.")
            String>
        perfilLinkedin = JsonNullable.undefined();

    @Size(min = 3, message = "Seu nome completo não pode ser tão curto...")
    private String nome;
}