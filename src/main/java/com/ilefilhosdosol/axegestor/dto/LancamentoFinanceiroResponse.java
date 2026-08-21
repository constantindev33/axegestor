package com.ilefilhosdosol.axegestor.dto;

import com.ilefilhosdosol.axegestor.enums.CategoriaFinanceira;
import com.ilefilhosdosol.axegestor.enums.StatusPagamento;
import com.ilefilhosdosol.axegestor.enums.TipoLancamento;
import com.ilefilhosdosol.axegestor.model.LancamentoFinanceiro;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoFinanceiroResponse(
        Long id,
        String descricao,
        String responsavel,
        BigDecimal valor,
        LocalDate dataLancamento,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        TipoLancamento tipo,
        CategoriaFinanceira categoria,
        StatusPagamento status,
        MembroResumoResponse membro,
        String observacoes
) {
    public static LancamentoFinanceiroResponse from(LancamentoFinanceiro lancamento) {
        return new LancamentoFinanceiroResponse(
                lancamento.getId(),
                lancamento.getDescricao(),
                lancamento.getResponsavel(),
                lancamento.getValor(),
                lancamento.getDataLancamento(),
                lancamento.getDataVencimento(),
                lancamento.getDataPagamento(),
                lancamento.getTipo(),
                lancamento.getCategoria(),
                lancamento.getStatus(),
                MembroResumoResponse.from(lancamento.getMembro()),
                lancamento.getObservacoes()
        );
    }
}
