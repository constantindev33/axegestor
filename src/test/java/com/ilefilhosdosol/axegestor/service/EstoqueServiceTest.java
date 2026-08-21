package com.ilefilhosdosol.axegestor.service;

import com.ilefilhosdosol.axegestor.enums.CategoriaMaterial;
import com.ilefilhosdosol.axegestor.enums.TipoMovimentacaoEstoque;
import com.ilefilhosdosol.axegestor.exception.BusinessException;
import com.ilefilhosdosol.axegestor.model.Material;
import com.ilefilhosdosol.axegestor.model.MovimentacaoEstoque;
import com.ilefilhosdosol.axegestor.repository.MaterialRepository;
import com.ilefilhosdosol.axegestor.repository.MovimentacaoEstoqueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private EstoqueService estoqueService;

    @Test
    void deveRejeitarMovimentacaoSemMaterial() {
        MovimentacaoEstoque movimentacao = MovimentacaoEstoque.builder()
                .tipo(TipoMovimentacaoEstoque.SAIDA)
                .quantidade(1)
                .responsavel("Teste")
                .build();

        assertThatThrownBy(() -> estoqueService.registrarMovimentacao(movimentacao))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Informe o id do material");

        verify(materialRepository, never()).save(any());
        verify(movimentacaoRepository, never()).save(any());
    }

    @Test
    void deveBloquearSaidaMaiorQueEstoqueDisponivel() {
        Material material = Material.builder()
                .id(1L)
                .nome("Vela branca")
                .categoria(CategoriaMaterial.VELA)
                .quantidadeAtual(2)
                .quantidadeMinima(1)
                .unidadeMedida("un")
                .build();

        MovimentacaoEstoque movimentacao = MovimentacaoEstoque.builder()
                .material(Material.builder().id(1L).build())
                .tipo(TipoMovimentacaoEstoque.SAIDA)
                .quantidade(3)
                .responsavel("Teste")
                .build();

        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));

        assertThatThrownBy(() -> estoqueService.registrarMovimentacao(movimentacao))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Estoque insuficiente para realizar a saida");

        verify(materialRepository, never()).save(any());
        verify(movimentacaoRepository, never()).save(any());
    }
}
