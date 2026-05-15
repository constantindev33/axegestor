package com.ilefilhosdosol.axegestor.repository;

import com.ilefilhosdosol.axegestor.enums.CategoriaFinanceira;
import com.ilefilhosdosol.axegestor.enums.StatusPagamento;
import com.ilefilhosdosol.axegestor.enums.TipoLancamento;
import com.ilefilhosdosol.axegestor.model.LancamentoFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LancamentoFinanceiroRepository extends JpaRepository<LancamentoFinanceiro, Long> {

    List<LancamentoFinanceiro> findByStatus(StatusPagamento status);

    List<LancamentoFinanceiro> findByTipo(TipoLancamento tipo);

    List<LancamentoFinanceiro> findByResponsavelContainingIgnoreCase(String responsavel);

    List<LancamentoFinanceiro> findByCategoria(CategoriaFinanceira categoria);

    List<LancamentoFinanceiro> findByDataVencimentoBeforeAndStatus(
            LocalDate data,
            StatusPagamento status
    );

    List<LancamentoFinanceiro> findByDataLancamentoBetween(
            LocalDate inicio,
            LocalDate fim
    );

    List<LancamentoFinanceiro> findByMembroId(Long membroId);

    List<LancamentoFinanceiro> findByMembroIdAndCategoriaAndDataLancamentoBetween(
            Long membroId,
            CategoriaFinanceira categoria,
            LocalDate inicio,
            LocalDate fim
    );
}
