package com.ilefilhosdosol.axegestor.controller;

import com.ilefilhosdosol.axegestor.dto.ResumoMensalidadeMembroResponse;
import com.ilefilhosdosol.axegestor.enums.CategoriaFinanceira;
import com.ilefilhosdosol.axegestor.enums.StatusPagamento;
import com.ilefilhosdosol.axegestor.enums.TipoLancamento;
import com.ilefilhosdosol.axegestor.exception.NotFoundException;
import com.ilefilhosdosol.axegestor.model.LancamentoFinanceiro;
import com.ilefilhosdosol.axegestor.model.Membro;
import com.ilefilhosdosol.axegestor.repository.LancamentoFinanceiroRepository;
import com.ilefilhosdosol.axegestor.repository.MembroRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
import java.util.List;

@RestController
@RequestMapping("/financeiro")
public class LancamentoFinanceiroController {

    private final LancamentoFinanceiroRepository repository;
    private final MembroRepository membroRepository;

    public LancamentoFinanceiroController(
            LancamentoFinanceiroRepository repository,
            MembroRepository membroRepository
    ) {
        this.repository = repository;
        this.membroRepository = membroRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LancamentoFinanceiro cadastrar(@RequestBody @Valid LancamentoFinanceiro lancamento) {
        return repository.save(lancamento);
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

        return repository.save(lancamento);
    }

    @PutMapping("/{id}/pagar")
    public LancamentoFinanceiro marcarComoPago(@PathVariable Long id) {
        LancamentoFinanceiro lancamento = buscarLancamento(id);

        lancamento.setStatus(StatusPagamento.PAGO);
        lancamento.setDataPagamento(LocalDate.now());

        return repository.save(lancamento);
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
}
