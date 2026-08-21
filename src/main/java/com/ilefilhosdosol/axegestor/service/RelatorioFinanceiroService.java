package com.ilefilhosdosol.axegestor.service;

import com.ilefilhosdosol.axegestor.dto.LancamentoFinanceiroResponse;
import com.ilefilhosdosol.axegestor.dto.RelatorioFinanceiroMensalResponse;
import com.ilefilhosdosol.axegestor.dto.ResumoMensalidadeMembroResponse;
import com.ilefilhosdosol.axegestor.dto.ResumoPorChaveResponse;
import com.ilefilhosdosol.axegestor.enums.StatusPagamento;
import com.ilefilhosdosol.axegestor.enums.TipoLancamento;
import com.ilefilhosdosol.axegestor.model.LancamentoFinanceiro;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RelatorioFinanceiroService {

    private final FinanceiroService financeiroService;

    public RelatorioFinanceiroService(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    public RelatorioFinanceiroMensalResponse montarResumoMensal(int ano, int mes) {
        List<LancamentoFinanceiro> lancamentos = financeiroService.relatorioMensalEntidades(ano, mes);
        List<ResumoMensalidadeMembroResponse> mensalidades = financeiroService.listarMensalidadesPorMembro(ano, mes);
        BigDecimal receitas = somarPorTipo(lancamentos, TipoLancamento.RECEITA);
        BigDecimal despesas = somarPorTipo(lancamentos, TipoLancamento.DESPESA);

        return new RelatorioFinanceiroMensalResponse(
                ano,
                mes,
                receitas,
                despesas,
                receitas.subtract(despesas),
                (long) lancamentos.size(),
                contarPorStatus(lancamentos, StatusPagamento.PAGO),
                contarPorStatus(lancamentos, StatusPagamento.PENDENTE),
                contarPorStatus(lancamentos, StatusPagamento.ATRASADO),
                contarMensalidades(mensalidades, "EM_DIA"),
                contarMensalidades(mensalidades, "ATRASADA"),
                contarMensalidades(mensalidades, "SEM_MENSALIDADE"),
                agruparPorCategoria(lancamentos),
                agruparPorStatus(lancamentos),
                mensalidades,
                lancamentos.stream()
                        .map(LancamentoFinanceiroResponse::from)
                        .toList()
        );
    }

    private BigDecimal somarPorTipo(List<LancamentoFinanceiro> lancamentos, TipoLancamento tipo) {
        return lancamentos.stream()
                .filter(lancamento -> lancamento.getTipo() == tipo)
                .map(LancamentoFinanceiro::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long contarPorStatus(List<LancamentoFinanceiro> lancamentos, StatusPagamento status) {
        return lancamentos.stream()
                .filter(lancamento -> lancamento.getStatus() == status)
                .count();
    }

    private Long contarMensalidades(List<ResumoMensalidadeMembroResponse> mensalidades, String status) {
        return mensalidades.stream()
                .filter(mensalidade -> status.equals(mensalidade.statusMensalidade()))
                .count();
    }

    private List<ResumoPorChaveResponse> agruparPorCategoria(List<LancamentoFinanceiro> lancamentos) {
        Map<String, List<LancamentoFinanceiro>> grupos = lancamentos.stream()
                .collect(Collectors.groupingBy(lancamento -> lancamento.getCategoria().name()));

        return grupos.entrySet().stream()
                .map(entry -> new ResumoPorChaveResponse(
                        entry.getKey(),
                        (long) entry.getValue().size(),
                        somarValores(entry.getValue())
                ))
                .sorted((a, b) -> b.total().compareTo(a.total()))
                .toList();
    }

    private List<ResumoPorChaveResponse> agruparPorStatus(List<LancamentoFinanceiro> lancamentos) {
        Map<String, List<LancamentoFinanceiro>> grupos = lancamentos.stream()
                .collect(Collectors.groupingBy(lancamento -> lancamento.getStatus().name()));

        return grupos.entrySet().stream()
                .map(entry -> new ResumoPorChaveResponse(
                        entry.getKey(),
                        (long) entry.getValue().size(),
                        somarValores(entry.getValue())
                ))
                .sorted((a, b) -> a.chave().compareTo(b.chave()))
                .toList();
    }

    private BigDecimal somarValores(List<LancamentoFinanceiro> lancamentos) {
        return lancamentos.stream()
                .map(LancamentoFinanceiro::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
