package com.ilefilhosdosol.axegestor.dto;

import com.ilefilhosdosol.axegestor.enums.TipoMovimentacaoEstoque;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovimentacaoEstoqueRequest(
        @Valid
        MaterialReferenciaRequest material,

        @NotNull(message = "Tipo de movimentação é obrigatório")
        TipoMovimentacaoEstoque tipo,

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade deve ser maior que zero")
        Integer quantidade,

        @NotBlank(message = "Responsável é obrigatório")
        @Size(max = 120, message = "Responsável deve ter no máximo 120 caracteres")
        String responsavel,

        @Size(max = 160, message = "Motivo deve ter no máximo 160 caracteres")
        String motivo,

        String observacoes
) {
}
