package com.ilefilhosdosol.axegestor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SessaoTratamentoRequest(
        @NotNull(message = "Número da sessão é obrigatório")
        @Min(value = 1, message = "Número da sessão deve ser maior que zero")
        Integer numeroSessao,

        @NotNull(message = "Data da sessão é obrigatória")
        LocalDate dataSessao,

        @NotNull(message = "Informe se a sessão foi realizada")
        Boolean realizada,

        String observacoes
) {
}
