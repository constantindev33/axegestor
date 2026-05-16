package com.ilefilhosdosol.axegestor.controller;

import com.ilefilhosdosol.axegestor.dto.RelatorioFinanceiroMensalResponse;
import com.ilefilhosdosol.axegestor.dto.ResumoMensalidadeMembroResponse;
import com.ilefilhosdosol.axegestor.dto.ResumoPorChaveResponse;
import com.ilefilhosdosol.axegestor.enums.CategoriaFinanceira;
import com.ilefilhosdosol.axegestor.enums.StatusPagamento;
import com.ilefilhosdosol.axegestor.enums.TipoLancamento;
import com.ilefilhosdosol.axegestor.exception.NotFoundException;
import com.ilefilhosdosol.axegestor.model.LancamentoFinanceiro;
import com.ilefilhosdosol.axegestor.model.Membro;
import com.ilefilhosdosol.axegestor.repository.LancamentoFinanceiroRepository;
import com.ilefilhosdosol.axegestor.repository.MembroRepository;
import com.ilefilhosdosol.axegestor.service.AuditoriaService;
import com.ilefilhosdosol.axegestor.service.RelatorioArquivoService;
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

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/financeiro")
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
public class LancamentoFinanceiroController {

    private final LancamentoFinanceiroRepository repository;
    private final MembroRepository membroRepository;
    private final AuditoriaService auditoriaService;
    private final RelatorioArquivoService relatorioArquivoService;

    public LancamentoFinanceiroController(
            LancamentoFinanceiroRepository repository,
            MembroRepository membroRepository,
            AuditoriaService auditoriaService,
            RelatorioArquivoService relatorioArquivoService
    ) {
        this.repository = repository;
        this.membroRepository = membroRepository;
        this.auditoriaService = auditoriaService;
        this.relatorioArquivoService = relatorioArquivoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LancamentoFinanceiro cadastrar(@RequestBody @Valid LancamentoFinanceiro lancamento) {
        LancamentoFinanceiro lancamentoSalvo = repository.save(lancamento);
        auditoriaService.registrar("FINANCEIRO", "CADASTRAR", "LancamentoFinanceiro", lancamentoSalvo.getId(), "Cadastrou lançamento " + lancamentoSalvo.getDescricao());
        return lancamentoSalvo;
    }

    @GetMapping
    public List<LancamentoFinanceiro> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public LancamentoFinanceiro buscarPorId(@PathVariable Long id) {
        return buscarLancamento(id);
    }

    @PutMapping("/{id}")
    public LancamentoFinanceiro atualizar(
            @PathVariable Long id,
            @RequestBody @Valid LancamentoFinanceiro lancamentoAtualizado
    ) {
        LancamentoFinanceiro lancamento = buscarLancamento(id);

        lancamento.setDescricao(lancamentoAtualizado.getDescricao());
        lancamento.setResponsavel(lancamentoAtualizado.getResponsavel());
        lancamento.setValor(lancamentoAtualizado.getValor());
        lancamento.setDataLancamento(lancamentoAtualizado.getDataLancamento());
        lancamento.setDataVencimento(lancamentoAtualizado.getDataVencimento());
        lancamento.setDataPagamento(lancamentoAtualizado.getDataPagamento());
        lancamento.setTipo(lancamentoAtualizado.getTipo());
        lancamento.setCategoria(lancamentoAtualizado.getCategoria());
        lancamento.setStatus(lancamentoAtualizado.getStatus());
        lancamento.setObservacoes(lancamentoAtualizado.getObservacoes());

        LancamentoFinanceiro lancamentoSalvo = repository.save(lancamento);
        auditoriaService.registrar("FINANCEIRO", "ATUALIZAR", "LancamentoFinanceiro", lancamentoSalvo.getId(), "Atualizou lançamento " + lancamentoSalvo.getDescricao());
        return lancamentoSalvo;
    }

    @PutMapping("/{id}/pagar")
    public LancamentoFinanceiro marcarComoPago(@PathVariable Long id) {
        LancamentoFinanceiro lancamento = buscarLancamento(id);

        lancamento.setStatus(StatusPagamento.PAGO);
        lancamento.setDataPagamento(LocalDate.now());

        LancamentoFinanceiro lancamentoSalvo = repository.save(lancamento);
        auditoriaService.registrar("FINANCEIRO", "PAGAR", "LancamentoFinanceiro", lancamentoSalvo.getId(), "Marcou como pago " + lancamentoSalvo.getDescricao());
        return lancamentoSalvo;
    }

    @GetMapping("/status")
    public List<LancamentoFinanceiro> buscarPorStatus(@RequestParam StatusPagamento status) {
        return repository.findByStatus(status);
    }

    @GetMapping("/tipo")
    public List<LancamentoFinanceiro> buscarPorTipo(@RequestParam TipoLancamento tipo) {
        return repository.findByTipo(tipo);
    }

    @GetMapping("/responsavel")
    public List<LancamentoFinanceiro> buscarPorResponsavel(@RequestParam String responsavel) {
        return repository.findByResponsavelContainingIgnoreCase(responsavel);
    }

    @GetMapping("/categoria")
    public List<LancamentoFinanceiro> buscarPorCategoria(@RequestParam CategoriaFinanceira categoria) {
        return repository.findByCategoria(categoria);
    }

    @GetMapping("/atrasados")
    public List<LancamentoFinanceiro> listarAtrasados() {
        return buscarPendentesVencidos();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Lançamento não encontrado");
        }

        repository.deleteById(id);
        auditoriaService.registrar("FINANCEIRO", "DELETAR", "LancamentoFinanceiro", id, "Deletou o lançamento de id " + id);
    }

    @GetMapping("/membro/{membroId}")
    public List<LancamentoFinanceiro> listarPorMembro(@PathVariable Long membroId) {
        return repository.findByMembroId(membroId);
    }

    @GetMapping("/relatorio-mensal")
    public List<LancamentoFinanceiro> relatorioMensal(
            @RequestParam int ano,
            @RequestParam int mes
    ) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        return repository.findByDataLancamentoBetween(inicio, fim);
    }

    @GetMapping("/relatorios/resumo-mensal")
    public RelatorioFinanceiroMensalResponse resumoMensal(
            @RequestParam int ano,
            @RequestParam int mes
    ) {
        return montarResumoMensal(ano, mes);
    }

    @GetMapping("/relatorios/resumo-mensal.xlsx")
    public ResponseEntity<byte[]> baixarResumoMensalExcel(
            @RequestParam int ano,
            @RequestParam int mes
    ) {
        byte[] arquivo = relatorioArquivoService.gerarExcel(montarResumoMensal(ano, mes));

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
        byte[] arquivo = relatorioArquivoService.gerarPdf(montarResumoMensal(ano, mes));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=prestacao-contas-axegestor-" + ano + "-" + mes + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(arquivo);
    }

    private RelatorioFinanceiroMensalResponse montarResumoMensal(int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());
        List<LancamentoFinanceiro> lancamentos = repository.findByDataLancamentoBetween(inicio, fim);
        List<ResumoMensalidadeMembroResponse> mensalidades = listarMensalidadesPorMembro(ano, mes);
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
                lancamentos
        );
    }

    @GetMapping("/alertas")
    public List<LancamentoFinanceiro> alertasFinanceiros() {
        return buscarPendentesVencidos();
    }

    @GetMapping("/mensalidades/membros")
    public List<ResumoMensalidadeMembroResponse> listarMensalidadesPorMembro(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes
    ) {
        LocalDate referencia = LocalDate.now();
        int anoConsulta = ano != null ? ano : referencia.getYear();
        int mesConsulta = mes != null ? mes : referencia.getMonthValue();
        LocalDate inicio = LocalDate.of(anoConsulta, mesConsulta, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        return membroRepository.findAll().stream()
                .map(membro -> montarResumoMensalidade(membro, inicio, fim))
                .toList();
    }

    private LancamentoFinanceiro buscarLancamento(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lançamento não encontrado"));
    }

    private List<LancamentoFinanceiro> buscarPendentesVencidos() {
        return repository.findByDataVencimentoBeforeAndStatus(
                LocalDate.now(),
                StatusPagamento.PENDENTE
        );
    }

    private ResumoMensalidadeMembroResponse montarResumoMensalidade(
            Membro membro,
            LocalDate inicio,
            LocalDate fim
    ) {
        LancamentoFinanceiro mensalidade = repository
                .findByMembroIdAndCategoriaAndDataLancamentoBetween(
                        membro.getId(),
                        CategoriaFinanceira.MENSALIDADE,
                        inicio,
                        fim
                )
                .stream()
                .findFirst()
                .orElse(null);

        if (mensalidade == null) {
            return new ResumoMensalidadeMembroResponse(
                    membro.getId(),
                    membro.getNome(),
                    membro.getTelefone(),
                    membro.getEmail(),
                    membro.getStatus() != null ? membro.getStatus().name() : "SEM_STATUS",
                    "SEM_MENSALIDADE",
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        return new ResumoMensalidadeMembroResponse(
                membro.getId(),
                membro.getNome(),
                membro.getTelefone(),
                membro.getEmail(),
                membro.getStatus() != null ? membro.getStatus().name() : "SEM_STATUS",
                definirStatusMensalidade(mensalidade),
                mensalidade.getId(),
                mensalidade.getValor(),
                mensalidade.getDataVencimento(),
                mensalidade.getDataPagamento(),
                mensalidade.getStatus()
        );
    }

    private String definirStatusMensalidade(LancamentoFinanceiro mensalidade) {
        if (mensalidade.getStatus() == StatusPagamento.PAGO) {
            return "EM_DIA";
        }

        if (mensalidade.getDataVencimento() != null
                && mensalidade.getDataVencimento().isBefore(LocalDate.now())
                && mensalidade.getStatus() == StatusPagamento.PENDENTE) {
            return "ATRASADA";
        }

        return mensalidade.getStatus().name();
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
