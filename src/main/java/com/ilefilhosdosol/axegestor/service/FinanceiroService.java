package com.ilefilhosdosol.axegestor.service;

import com.ilefilhosdosol.axegestor.dto.LancamentoFinanceiroRequest;
import com.ilefilhosdosol.axegestor.dto.LancamentoFinanceiroResponse;
import com.ilefilhosdosol.axegestor.dto.ResumoMensalidadeMembroResponse;
import com.ilefilhosdosol.axegestor.enums.CategoriaFinanceira;
import com.ilefilhosdosol.axegestor.enums.StatusPagamento;
import com.ilefilhosdosol.axegestor.enums.TipoLancamento;
import com.ilefilhosdosol.axegestor.exception.BusinessException;
import com.ilefilhosdosol.axegestor.exception.NotFoundException;
import com.ilefilhosdosol.axegestor.model.LancamentoFinanceiro;
import com.ilefilhosdosol.axegestor.model.Membro;
import com.ilefilhosdosol.axegestor.repository.LancamentoFinanceiroRepository;
import com.ilefilhosdosol.axegestor.repository.MembroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class FinanceiroService {

    private static final int ANO_MINIMO = 2000;
    private static final int ANO_MAXIMO = 2100;

    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final MembroRepository membroRepository;
    private final AuditoriaService auditoriaService;

    public FinanceiroService(
            LancamentoFinanceiroRepository lancamentoRepository,
            MembroRepository membroRepository,
            AuditoriaService auditoriaService
    ) {
        this.lancamentoRepository = lancamentoRepository;
        this.membroRepository = membroRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public LancamentoFinanceiroResponse cadastrar(LancamentoFinanceiroRequest request) {
        LancamentoFinanceiro lancamento = montarLancamento(request);
        LancamentoFinanceiro lancamentoSalvo = lancamentoRepository.save(lancamento);
        auditoriaService.registrar("FINANCEIRO", "CADASTRAR", "LancamentoFinanceiro", lancamentoSalvo.getId(), "Cadastrou lancamento " + lancamentoSalvo.getDescricao());
        return LancamentoFinanceiroResponse.from(lancamentoSalvo);
    }

    public List<LancamentoFinanceiroResponse> listar() {
        return mapearLista(lancamentoRepository.findAll());
    }

    public LancamentoFinanceiroResponse buscarPorId(Long id) {
        return LancamentoFinanceiroResponse.from(buscarLancamento(id));
    }

    @Transactional
    public LancamentoFinanceiroResponse atualizar(Long id, LancamentoFinanceiroRequest request) {
        LancamentoFinanceiro lancamento = buscarLancamento(id);

        aplicarDados(lancamento, request);

        LancamentoFinanceiro lancamentoSalvo = lancamentoRepository.save(lancamento);
        auditoriaService.registrar("FINANCEIRO", "ATUALIZAR", "LancamentoFinanceiro", lancamentoSalvo.getId(), "Atualizou lancamento " + lancamentoSalvo.getDescricao());
        return LancamentoFinanceiroResponse.from(lancamentoSalvo);
    }

    @Transactional
    public LancamentoFinanceiroResponse marcarComoPago(Long id) {
        LancamentoFinanceiro lancamento = buscarLancamento(id);

        if (lancamento.getStatus() == StatusPagamento.CANCELADO) {
            throw new BusinessException("Lancamento cancelado nao pode ser marcado como pago");
        }

        lancamento.setStatus(StatusPagamento.PAGO);
        lancamento.setDataPagamento(LocalDate.now());

        LancamentoFinanceiro lancamentoSalvo = lancamentoRepository.save(lancamento);
        auditoriaService.registrar("FINANCEIRO", "PAGAR", "LancamentoFinanceiro", lancamentoSalvo.getId(), "Marcou como pago " + lancamentoSalvo.getDescricao());
        return LancamentoFinanceiroResponse.from(lancamentoSalvo);
    }

    public List<LancamentoFinanceiroResponse> buscarPorStatus(StatusPagamento status) {
        return mapearLista(lancamentoRepository.findByStatus(status));
    }

    public List<LancamentoFinanceiroResponse> buscarPorTipo(TipoLancamento tipo) {
        return mapearLista(lancamentoRepository.findByTipo(tipo));
    }

    public List<LancamentoFinanceiroResponse> buscarPorResponsavel(String responsavel) {
        return mapearLista(lancamentoRepository.findByResponsavelContainingIgnoreCase(responsavel));
    }

    public List<LancamentoFinanceiroResponse> buscarPorCategoria(CategoriaFinanceira categoria) {
        return mapearLista(lancamentoRepository.findByCategoria(categoria));
    }

    public List<LancamentoFinanceiroResponse> listarAtrasados() {
        return mapearLista(lancamentoRepository.findByDataVencimentoBeforeAndStatus(LocalDate.now(), StatusPagamento.PENDENTE));
    }

    @Transactional
    public void deletar(Long id) {
        if (!lancamentoRepository.existsById(id)) {
            throw new NotFoundException("Lancamento nao encontrado");
        }

        lancamentoRepository.deleteById(id);
        auditoriaService.registrar("FINANCEIRO", "DELETAR", "LancamentoFinanceiro", id, "Deletou o lancamento de id " + id);
    }

    public List<LancamentoFinanceiroResponse> listarPorMembro(Long membroId) {
        if (!membroRepository.existsById(membroId)) {
            throw new NotFoundException("Membro nao encontrado");
        }

        return mapearLista(lancamentoRepository.findByMembroId(membroId));
    }

    public List<LancamentoFinanceiroResponse> relatorioMensal(int ano, int mes) {
        return mapearLista(relatorioMensalEntidades(ano, mes));
    }

    List<LancamentoFinanceiro> relatorioMensalEntidades(int ano, int mes) {
        YearMonth periodo = validarPeriodo(ano, mes);
        return lancamentoRepository.findByDataLancamentoBetween(
                periodo.atDay(1),
                periodo.atEndOfMonth()
        );
    }

    public List<ResumoMensalidadeMembroResponse> listarMensalidadesPorMembro(Integer ano, Integer mes) {
        YearMonth periodo = resolverPeriodo(ano, mes);

        return membroRepository.findAll().stream()
                .map(membro -> montarResumoMensalidade(membro, periodo))
                .toList();
    }

    YearMonth validarPeriodo(int ano, int mes) {
        if (ano < ANO_MINIMO || ano > ANO_MAXIMO) {
            throw new BusinessException("Ano deve estar entre " + ANO_MINIMO + " e " + ANO_MAXIMO);
        }

        if (mes < 1 || mes > 12) {
            throw new BusinessException("Mes deve estar entre 1 e 12");
        }

        return YearMonth.of(ano, mes);
    }

    private YearMonth resolverPeriodo(Integer ano, Integer mes) {
        LocalDate referencia = LocalDate.now();
        int anoConsulta = ano != null ? ano : referencia.getYear();
        int mesConsulta = mes != null ? mes : referencia.getMonthValue();
        return validarPeriodo(anoConsulta, mesConsulta);
    }

    private LancamentoFinanceiro buscarLancamento(Long id) {
        return lancamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lancamento nao encontrado"));
    }

    private LancamentoFinanceiro montarLancamento(LancamentoFinanceiroRequest request) {
        LancamentoFinanceiro lancamento = new LancamentoFinanceiro();
        aplicarDados(lancamento, request);
        return lancamento;
    }

    private void aplicarDados(LancamentoFinanceiro lancamento, LancamentoFinanceiroRequest request) {
        lancamento.setDescricao(request.descricao());
        lancamento.setResponsavel(request.responsavel());
        lancamento.setValor(request.valor());
        lancamento.setDataLancamento(request.dataLancamento());
        lancamento.setDataVencimento(request.dataVencimento());
        lancamento.setDataPagamento(request.dataPagamento());
        lancamento.setTipo(request.tipo());
        lancamento.setCategoria(request.categoria());
        lancamento.setStatus(request.status());
        lancamento.setMembro(buscarMembroDoRequest(request));
        lancamento.setObservacoes(request.observacoes());
    }

    private Membro buscarMembroDoRequest(LancamentoFinanceiroRequest request) {
        if (request.membro() == null || request.membro().id() == null) {
            return null;
        }

        return membroRepository.findById(request.membro().id())
                .orElseThrow(() -> new NotFoundException("Membro nao encontrado"));
    }

    private List<LancamentoFinanceiroResponse> mapearLista(List<LancamentoFinanceiro> lancamentos) {
        return lancamentos.stream()
                .map(LancamentoFinanceiroResponse::from)
                .toList();
    }

    private ResumoMensalidadeMembroResponse montarResumoMensalidade(Membro membro, YearMonth periodo) {
        LancamentoFinanceiro mensalidade = lancamentoRepository
                .findByMembroIdAndCategoriaAndDataLancamentoBetween(
                        membro.getId(),
                        CategoriaFinanceira.MENSALIDADE,
                        periodo.atDay(1),
                        periodo.atEndOfMonth()
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
