package com.ilefilhosdosol.axegestor.dto;

import java.math.BigDecimal;

public record ResumoPorChaveResponse(
        String chave,
        Long quantidade,
        BigDecimal total
) {
}
