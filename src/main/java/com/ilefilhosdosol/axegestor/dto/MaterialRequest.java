package com.ilefilhosdosol.axegestor.dto;

import com.ilefilhosdosol.axegestor.enums.CategoriaMaterial;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MaterialRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nome,

        @NotNull(message = "Categoria é obrigatória")
        CategoriaMaterial categoria,

        @NotNull(message = "Quantidade atual é obrigatória")
        @Min(value = 0, message = "Quantidade atual não pode ser negativa")
        Integer quantidadeAtual,

        @NotNull(message = "Quantidade mínima é obrigatória")
        @Min(value = 0, message = "Quantidade mínima não pode ser negativa")
        Integer quantidadeMinima,

        @NotBlank(message = "Unidade de medida é obrigatória")
        @Size(max = 30, message = "Unidade de medida deve ter no máximo 30 caracteres")
        String unidadeMedida,

        @Size(max = 120, message = "Local de armazenamento deve ter no máximo 120 caracteres")
        String localArmazenamento,

        String observacoes
) {
}
