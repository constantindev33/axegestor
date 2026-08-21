package com.ilefilhosdosol.axegestor.service;

import com.ilefilhosdosol.axegestor.enums.StatusAssistencia;
import com.ilefilhosdosol.axegestor.exception.BusinessException;
import com.ilefilhosdosol.axegestor.model.Assistencia;
import com.ilefilhosdosol.axegestor.model.SessaoTratamento;
import com.ilefilhosdosol.axegestor.repository.AssistenciaRepository;
import com.ilefilhosdosol.axegestor.repository.SessaoTratamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistenciaServiceTest {

    @Mock
    private AssistenciaRepository assistenciaRepository;

    @Mock
    private SessaoTratamentoRepository sessaoTratamentoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private AssistenciaService assistenciaService;

    @Test
    void naoDeveAtualizarSessaoDeOutraAssistencia() {
        Assistencia assistenciaDaUrl = Assistencia.builder()
                .id(1L)
                .nome("Pessoa A")
                .status(StatusAssistencia.EM_ANDAMENTO)
                .build();

        Assistencia outraAssistencia = Assistencia.builder()
                .id(2L)
                .nome("Pessoa B")
                .status(StatusAssistencia.EM_ANDAMENTO)
                .build();

        SessaoTratamento sessao = SessaoTratamento.builder()
                .id(10L)
                .numeroSessao(1)
                .dataSessao(LocalDate.now())
                .realizada(false)
                .assistencia(outraAssistencia)
                .build();

        SessaoTratamento atualizacao = SessaoTratamento.builder()
                .numeroSessao(2)
                .dataSessao(LocalDate.now())
                .realizada(true)
                .build();

        when(assistenciaRepository.findById(1L)).thenReturn(Optional.of(assistenciaDaUrl));
        when(sessaoTratamentoRepository.findById(10L)).thenReturn(Optional.of(sessao));

        assertThatThrownBy(() -> assistenciaService.atualizarSessao(1L, 10L, atualizacao))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Sessao nao pertence a assistencia informada");

        verify(sessaoTratamentoRepository, never()).save(any());
    }
}
