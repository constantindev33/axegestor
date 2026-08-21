package com.ilefilhosdosol.axegestor.dto;

import com.ilefilhosdosol.axegestor.enums.CategoriaFinanceira;
import com.ilefilhosdosol.axegestor.enums.StatusPagamento;
import com.ilefilhosdosol.axegestor.enums.TipoLancamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoFinanceiroRequest(
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 160, message = "Descrição deve ter no máximo 160 caracteres")
        String descricao,

        @Size(max = 120, message = "Responsável deve ter no máximo 120 caracteres")
        String responsavel,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "Data de lançamento é obrigatória")
        LocalDate dataLancamento,

        LocalDate dataVencimento,
        LocalDate dataPagamento,

        @NotNull(message = "Tipo é obrigatório")
        TipoLancamento tipo,

        @NotNull(message = "Categoria é obrigatória")
        CategoriaFinanceira categoria,

        @NotNull(message = "Status é obrigatório")
        StatusPagamento status,

        @Valid
        MembroReferenciaRequest membro,

        String observacoes
) {
}
