package com.ilefilhosdosol.axegestor.dto;

import com.ilefilhosdosol.axegestor.enums.CategoriaMaterial;
import com.ilefilhosdosol.axegestor.model.Material;

public record MaterialResponse(
        Long id,
        String nome,
        CategoriaMaterial categoria,
        Integer quantidadeAtual,
        Integer quantidadeMinima,
        String unidadeMedida,
        String localArmazenamento,
        String observacoes
) {
    public static MaterialResponse from(Material material) {
        return new MaterialResponse(
                material.getId(),
                material.getNome(),
                material.getCategoria(),
                material.getQuantidadeAtual(),
                material.getQuantidadeMinima(),
                material.getUnidadeMedida(),
                material.getLocalArmazenamento(),
                material.getObservacoes()
        );
    }
}
