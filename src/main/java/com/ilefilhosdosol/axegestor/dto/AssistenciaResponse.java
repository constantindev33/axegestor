package com.ilefilhosdosol.axegestor.dto;

import com.ilefilhosdosol.axegestor.enums.StatusAssistencia;
import com.ilefilhosdosol.axegestor.enums.TipoTratamento;
import com.ilefilhosdosol.axegestor.model.Assistencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record AssistenciaResponse(
        Long id,
        String nome,
        String entidadeConsulta,
        LocalDate dataConsulta,
        String cidade,
        String whatsapp,
        String observacoes,
        StatusAssistencia status,
        Set<TipoTratamento> tratamentos,
        List<SessaoTratamentoResponse> sessoes
) {
    public static AssistenciaResponse from(Assistencia assistencia) {
        List<SessaoTratamentoResponse> sessoes = assistencia.getSessoes() == null
                ? List.of()
                : assistencia.getSessoes().stream()
                        .map(SessaoTratamentoResponse::from)
                        .toList();

        return new AssistenciaResponse(
                assistencia.getId(),
                assistencia.getNome(),
                assistencia.getEntidadeConsulta(),
                assistencia.getDataConsulta(),
                assistencia.getCidade(),
                assistencia.getWhatsapp(),
                assistencia.getObservacoes(),
                assistencia.getStatus(),
                assistencia.getTratamentos(),
                sessoes
        );
    }
}
