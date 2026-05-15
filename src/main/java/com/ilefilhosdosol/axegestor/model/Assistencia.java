package com.ilefilhosdosol.axegestor.model;

import com.ilefilhosdosol.axegestor.enums.StatusAssistencia;
import com.ilefilhosdosol.axegestor.enums.TipoTratamento;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "assistencias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    private String nome;

    @Size(max = 120, message = "Entidade de consulta deve ter no máximo 120 caracteres")
    private String entidadeConsulta;

    private LocalDate dataConsulta;

    @Size(max = 120, message = "Cidade deve ter no máximo 120 caracteres")
    private String cidade;

    @Size(max = 30, message = "WhatsApp deve ter no máximo 30 caracteres")
    private String whatsapp;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    private StatusAssistencia status;

    @ElementCollection(targetClass = TipoTratamento.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "assistencia_tratamentos",
            joinColumns = @JoinColumn(name = "assistencia_id")
    )
    @Column(name = "tipo_tratamento")
    private Set<TipoTratamento> tratamentos;

    @OneToMany(
            mappedBy = "assistencia",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Valid
    private List<SessaoTratamento> sessoes;
}
