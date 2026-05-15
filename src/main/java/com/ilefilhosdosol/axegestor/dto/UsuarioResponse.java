package com.ilefilhosdosol.axegestor.dto;

import com.ilefilhosdosol.axegestor.enums.PerfilUsuario;
import com.ilefilhosdosol.axegestor.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        Boolean ativo
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getAtivo()
        );
    }
}
