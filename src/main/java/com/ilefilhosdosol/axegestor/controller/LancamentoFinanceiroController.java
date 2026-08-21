package com.ilefilhosdosol.axegestor.controller;

import com.ilefilhosdosol.axegestor.dto.RelatorioFinanceiroMensalResponse;
import com.ilefilhosdosol.axegestor.dto.ResumoMensalidadeMembroResponse;
import com.ilefilhosdosol.axegestor.enums.CategoriaFinanceira;
import com.ilefilhosdosol.axegestor.enums.StatusPagamento;
import com.ilefilhosdosol.axegestor.enums.TipoLancamento;
import com.ilefilhosdosol.axegestor.model.LancamentoFinanceiro;
import com.ilefilhosdosol.axegestor.service.FinanceiroService;
import com.ilefilhosdosol.axegestor.service.RelatorioArquivoService;
import com.ilefilhosdosol.axegestor.service.RelatorioFinanceiroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/financeiro")
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
public class LancamentoFinanceiroController {

    private final FinanceiroService financeiroService;
    private final RelatorioFinanceiroService relatorioFinanceiroService;
    private final RelatorioArquivoService relatorioArquivoService;

    public LancamentoFinanceiroController(
            FinanceiroService financeiroService,
            RelatorioFinanceiroService relatorioFinanceiroService,
            RelatorioArquivoService relatorioArquivoService
    ) {
        this.financeiroService = financeiroService;
        this.relatorioFinanceiroService = relatorioFinanceiroService;
        this.relatorioArquivoService = relatorioArquivoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LancamentoFinanceiro cadastrar(@RequestBody @Valid LancamentoFinanceiro lancamento) {
        return financeiroService.cadastrar(lancamento);
    }

    @GetMapping
    public List<LancamentoFinanceiro> listar() {
        return financeiroService.listar();
    }

    @GetMapping("/{id}")
    public LancamentoFinanceiro buscarPorId(@PathVariable Long id) {
        return financeiroService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public LancamentoFinanceiro atualizar(
            @PathVariable Long id,
            @RequestBody @Valid LancamentoFinanceiro lancamentoAtualizado
    ) {
        return financeiroService.atualizar(id, lancamentoAtualizado);
    }

    @PutMapping("/{id}/pagar")
    public LancamentoFinanceiro marcarComoPago(@PathVariable Long id) {
        return financeiroService.marcarComoPago(id);
    }

    @GetMapping("/status")
    public List<LancamentoFinanceiro> buscarPorStatus(@RequestParam StatusPagamento status) {
        return financeiroService.buscarPorStatus(status);
    }

    @GetMapping("/tipo")
    public List<LancamentoFinanceiro> buscarPorTipo(@RequestParam TipoLancamento tipo) {
        return financeiroService.buscarPorTipo(tipo);
    }

    @GetMapping("/responsavel")
    public List<LancamentoFinanceiro> buscarPorResponsavel(@RequestParam String responsavel) {
        return financeiroService.buscarPorResponsavel(responsavel);
    }

    @GetMapping("/categoria")
    public List<LancamentoFinanceiro> buscarPorCategoria(@RequestParam CategoriaFinanceira categoria) {
        return financeiroService.buscarPorCategoria(categoria);
    }

    @GetMapping("/atrasados")
    public List<LancamentoFinanceiro> listarAtrasados() {
        return financeiroService.listarAtrasados();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        financeiroService.deletar(id);
    }

    @GetMapping("/membro/{membroId}")
    public List<LancamentoFinanceiro> listarPorMembro(@PathVariable Long membroId) {
        return financeiroService.listarPorMembro(membroId);
    }

    @GetMapping("/relatorio-mensal")
    public List<LancamentoFinanceiro> relatorioMensal(
            @RequestParam int ano,
            @RequestParam int mes
    ) {
        return financeiroService.relatorioMensal(ano, mes);
    }

    @GetMapping("/relatorios/resumo-mensal")
    public RelatorioFinanceiroMensalResponse resumoMensal(
            @RequestParam int ano,
            @RequestParam int mes
    ) {
        return relatorioFinanceiroService.montarResumoMensal(ano, mes);
    }

    @GetMapping("/relatorios/resumo-mensal.xlsx")
    public ResponseEntity<byte[]> baixarResumoMensalExcel(
            @RequestParam int ano,
            @RequestParam int mes
    ) {
        byte[] arquivo = relatorioArquivoService.gerarExcel(relatorioFinanceiroService.montarResumoMensal(ano, mes));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-axegestor-" + ano + "-" + mes + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }

    @GetMapping("/relatorios/resumo-mensal.pdf")
    public ResponseEntity<byte[]> baixarResumoMensalPdf(
            @RequestParam int ano,
            @RequestParam int mes
    ) {
        byte[] arquivo = relatorioArquivoService.gerarPdf(relatorioFinanceiroService.montarResumoMensal(ano, mes));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=prestacao-contas-axegestor-" + ano + "-" + mes + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(arquivo);
    }

    @GetMapping("/alertas")
    public List<LancamentoFinanceiro> alertasFinanceiros() {
        return financeiroService.listarAtrasados();
    }

    @GetMapping("/mensalidades/membros")
    public List<ResumoMensalidadeMembroResponse> listarMensalidadesPorMembro(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes
    ) {
        return financeiroService.listarMensalidadesPorMembro(ano, mes);
    }
}
