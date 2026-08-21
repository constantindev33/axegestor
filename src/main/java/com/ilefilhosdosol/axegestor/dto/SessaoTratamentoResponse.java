package com.ilefilhosdosol.axegestor.dto;

import com.ilefilhosdosol.axegestor.model.SessaoTratamento;

import java.time.LocalDate;

public record SessaoTratamentoResponse(
        Long id,
        Integer numeroSessao,
        LocalDate dataSessao,
        Boolean realizada,
        String observacoes
) {
    public static SessaoTratamentoResponse from(SessaoTratamento sessao) {
        return new SessaoTratamentoResponse(
                sessao.getId(),
                sessao.getNumeroSessao(),
                sessao.getDataSessao(),
                sessao.getRealizada(),
                sessao.getObservacoes()
        );
    }
}
