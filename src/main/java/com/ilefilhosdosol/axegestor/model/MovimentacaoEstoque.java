package com.ilefilhosdosol.axegestor.model;

import com.ilefilhosdosol.axegestor.enums.TipoMovimentacaoEstoque;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "movimentacoes_estoque")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "material_id")
    @NotNull(message = "Material é obrigatório")
    private Material material;

    @NotNull(message = "Tipo de movimentação é obrigatório")
    @Enumerated(EnumType.STRING)
    private TipoMovimentacaoEstoque tipo;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantidade;

    private LocalDate dataMovimentacao;

    @NotBlank(message = "Responsável é obrigatório")
    @Size(max = 120, message = "Responsável deve ter no máximo 120 caracteres")
    private String responsavel;

    @Size(max = 160, message = "Motivo deve ter no máximo 160 caracteres")
    private String motivo;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}
