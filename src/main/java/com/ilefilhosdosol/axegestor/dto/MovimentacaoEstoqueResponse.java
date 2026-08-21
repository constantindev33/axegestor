package com.ilefilhosdosol.axegestor.dto;

import com.ilefilhosdosol.axegestor.enums.TipoMovimentacaoEstoque;
import com.ilefilhosdosol.axegestor.model.MovimentacaoEstoque;

import java.time.LocalDate;

public record MovimentacaoEstoqueResponse(
        Long id,
        MaterialResponse material,
        TipoMovimentacaoEstoque tipo,
        Integer quantidade,
        LocalDate dataMovimentacao,
        String responsavel,
        String motivo,
        String observacoes
) {
    public static MovimentacaoEstoqueResponse from(MovimentacaoEstoque movimentacao) {
        return new MovimentacaoEstoqueResponse(
                movimentacao.getId(),
                MaterialResponse.from(movimentacao.getMaterial()),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getDataMovimentacao(),
                movimentacao.getResponsavel(),
                movimentacao.getMotivo(),
                movimentacao.getObservacoes()
        );
    }
}
