package com.ilefilhosdosol.axegestor.dto;

import com.ilefilhosdosol.axegestor.enums.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumoMensalidadeMembroResponse(
        Long membroId,
        String nome,
        String telefone,
        String email,
        String statusMembro,
        String statusMensalidade,
        Long lancamentoId,
        BigDecimal valor,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        StatusPagamento statusPagamento
) {
}
