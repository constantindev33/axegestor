package com.ilefilhosdosol.axegestor.dto;

import com.ilefilhosdosol.axegestor.model.Membro;

public record MembroResumoResponse(
        Long id,
        String nome,
        String telefone,
        String email
) {
    public static MembroResumoResponse from(Membro membro) {
        if (membro == null) {
            return null;
        }

        return new MembroResumoResponse(
                membro.getId(),
                membro.getNome(),
                membro.getTelefone(),
                membro.getEmail()
        );
    }
}
