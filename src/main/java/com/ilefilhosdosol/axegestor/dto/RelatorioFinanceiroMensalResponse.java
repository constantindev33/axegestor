package com.ilefilhosdosol.axegestor.dto;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioFinanceiroMensalResponse(
        Integer ano,
        Integer mes,
        BigDecimal receitas,
        BigDecimal despesas,
        BigDecimal saldo,
        Long totalLancamentos,
        Long totalPagos,
        Long totalPendentes,
        Long totalAtrasados,
        Long mensalidadesEmDia,
        Long mensalidadesAtrasadas,
        Long mensalidadesSemCadastro,
        List<ResumoPorChaveResponse> porCategoria,
        List<ResumoPorChaveResponse> porStatus,
        List<ResumoMensalidadeMembroResponse> mensalidades,
        List<LancamentoFinanceiroResponse> lancamentos
) {
}
