package com.ilefilhosdosol.axegestor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "sessoes_tratamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessaoTratamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Número da sessão é obrigatório")
    @Min(value = 1, message = "Número da sessão deve ser maior que zero")
    private Integer numeroSessao;

    @NotNull(message = "Data da sessão é obrigatória")
    private LocalDate dataSessao;

    @NotNull(message = "Informe se a sessão foi realizada")
    private Boolean realizada;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "assistencia_id")
    @JsonIgnore
    private Assistencia assistencia;
}
