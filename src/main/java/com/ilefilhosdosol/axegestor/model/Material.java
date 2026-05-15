package com.ilefilhosdosol.axegestor.model;

import com.ilefilhosdosol.axegestor.enums.CategoriaMaterial;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "materiais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    private String nome;

    @NotNull(message = "Categoria é obrigatória")
    @Enumerated(EnumType.STRING)
    private CategoriaMaterial categoria;

    @NotNull(message = "Quantidade atual é obrigatória")
    @Min(value = 0, message = "Quantidade atual não pode ser negativa")
    private Integer quantidadeAtual;

    @NotNull(message = "Quantidade mínima é obrigatória")
    @Min(value = 0, message = "Quantidade mínima não pode ser negativa")
    private Integer quantidadeMinima;

    @NotBlank(message = "Unidade de medida é obrigatória")
    @Size(max = 30, message = "Unidade de medida deve ter no máximo 30 caracteres")
    private String unidadeMedida;

    @Size(max = 120, message = "Local de armazenamento deve ter no máximo 120 caracteres")
    private String localArmazenamento;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}
