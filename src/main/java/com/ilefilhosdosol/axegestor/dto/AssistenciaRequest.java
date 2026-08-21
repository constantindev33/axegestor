package com.ilefilhosdosol.axegestor.dto;

import com.ilefilhosdosol.axegestor.enums.StatusAssistencia;
import com.ilefilhosdosol.axegestor.enums.TipoTratamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record AssistenciaRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nome,

        @Size(max = 120, message = "Entidade de consulta deve ter no máximo 120 caracteres")
        String entidadeConsulta,

        LocalDate dataConsulta,

        @Size(max = 120, message = "Cidade deve ter no máximo 120 caracteres")
        String cidade,

        @Size(max = 30, message = "WhatsApp deve ter no máximo 30 caracteres")
        String whatsapp,

        String observacoes,

        @NotNull(message = "Status é obrigatório")
        StatusAssistencia status,

        Set<TipoTratamento> tratamentos,

        @Valid
        List<SessaoTratamentoRequest> sessoes
) {
}
