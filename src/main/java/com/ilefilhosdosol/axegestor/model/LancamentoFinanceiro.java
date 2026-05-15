package com.ilefilhosdosol.axegestor.model;

import com.ilefilhosdosol.axegestor.enums.CategoriaFinanceira;
import com.ilefilhosdosol.axegestor.enums.StatusPagamento;
import com.ilefilhosdosol.axegestor.enums.TipoLancamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.ilefilhosdosol.axegestor.model.Membro;

@Entity
@Table(name = "lancamentos_financeiros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LancamentoFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 160, message = "Descrição deve ter no máximo 160 caracteres")
    private String descricao;

    @Size(max = 120, message = "Responsável deve ter no máximo 120 caracteres")
    private String responsavel;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "Data de lançamento é obrigatória")
    private LocalDate dataLancamento;

    private LocalDate dataVencimento;

    private LocalDate dataPagamento;

    @NotNull(message = "Tipo é obrigatório")
    @Enumerated(EnumType.STRING)
    private TipoLancamento tipo;

    @NotNull(message = "Categoria é obrigatória")
    @Enumerated(EnumType.STRING)
    private CategoriaFinanceira categoria;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    private StatusPagamento status;

    @ManyToOne
    @JoinColumn(name = "membro_id")
    private Membro membro;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}
