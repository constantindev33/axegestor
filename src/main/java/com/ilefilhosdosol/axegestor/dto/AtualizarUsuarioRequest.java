package com.ilefilhosdosol.axegestor.dto;

import com.ilefilhosdosol.axegestor.enums.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarUsuarioRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail deve ser válido")
        @Size(max = 160, message = "E-mail deve ter no máximo 160 caracteres")
        String email,

        @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres")
        String senha,

        @NotNull(message = "Perfil é obrigatório")
        PerfilUsuario perfil,

        @NotNull(message = "Status ativo é obrigatório")
        Boolean ativo
) {
}
