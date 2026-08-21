package com.ilefilhosdosol.axegestor.service;

import com.ilefilhosdosol.axegestor.dto.ResumoMensalidadeMembroResponse;
import com.ilefilhosdosol.axegestor.enums.CategoriaFinanceira;
import com.ilefilhosdosol.axegestor.enums.FuncaoMembro;
import com.ilefilhosdosol.axegestor.enums.StatusMembro;
import com.ilefilhosdosol.axegestor.enums.StatusPagamento;
import com.ilefilhosdosol.axegestor.enums.TipoLancamento;
import com.ilefilhosdosol.axegestor.exception.BusinessException;
import com.ilefilhosdosol.axegestor.model.LancamentoFinanceiro;
import com.ilefilhosdosol.axegestor.model.Membro;
import com.ilefilhosdosol.axegestor.repository.LancamentoFinanceiroRepository;
import com.ilefilhosdosol.axegestor.repository.MembroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceiroServiceTest {

    @Mock
    private LancamentoFinanceiroRepository lancamentoRepository;

    @Mock
    private MembroRepository membroRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private FinanceiroService financeiroService;

    @Test
    void deveRejeitarMesInvalidoNoRelatorioMensal() {
        assertThatThrownBy(() -> financeiroService.relatorioMensal(2026, 13))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Mes deve estar entre 1 e 12");
    }

    @Test
    void deveRejeitarAnoForaDoIntervalo() {
        assertThatThrownBy(() -> financeiroService.relatorioMensal(1800, 5))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ano deve estar entre 2000 e 2100");
    }

    @Test
    void naoDeveMarcarLancamentoCanceladoComoPago() {
        LancamentoFinanceiro lancamento = LancamentoFinanceiro.builder()
                .id(1L)
                .descricao("Mensalidade")
                .valor(BigDecimal.TEN)
                .dataLancamento(LocalDate.now())
                .tipo(TipoLancamento.RECEITA)
                .categoria(CategoriaFinanceira.MENSALIDADE)
                .status(StatusPagamento.CANCELADO)
                .build();

        when(lancamentoRepository.findById(1L)).thenReturn(Optional.of(lancamento));

        assertThatThrownBy(() -> financeiroService.marcarComoPago(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Lancamento cancelado nao pode ser marcado como pago");

        verify(lancamentoRepository, never()).save(any());
    }

    @Test
    void deveIdentificarMensalidadeAtrasada() {
        Membro membro = Membro.builder()
                .id(1L)
                .nome("Membro Teste")
                .telefone("11999999999")
                .email("membro@teste.local")
                .funcao(FuncaoMembro.ESTUDANTE)
                .status(StatusMembro.ATIVO)
                .build();

        LancamentoFinanceiro mensalidade = LancamentoFinanceiro.builder()
                .id(10L)
                .descricao("Mensalidade")
                .valor(BigDecimal.valueOf(50))
                .dataLancamento(LocalDate.of(2026, 5, 1))
                .dataVencimento(LocalDate.now().minusDays(1))
                .tipo(TipoLancamento.RECEITA)
                .categoria(CategoriaFinanceira.MENSALIDADE)
                .status(StatusPagamento.PENDENTE)
                .build();

        when(membroRepository.findAll()).thenReturn(List.of(membro));
        when(lancamentoRepository.findByMembroIdAndCategoriaAndDataLancamentoBetween(
                1L,
                CategoriaFinanceira.MENSALIDADE,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31)
        )).thenReturn(List.of(mensalidade));

        List<ResumoMensalidadeMembroResponse> mensalidades = financeiroService.listarMensalidadesPorMembro(2026, 5);

        assertThat(mensalidades).hasSize(1);
        assertThat(mensalidades.get(0).statusMensalidade()).isEqualTo("ATRASADA");
    }
}
