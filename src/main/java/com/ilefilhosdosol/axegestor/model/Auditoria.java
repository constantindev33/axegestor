package com.ilefilhosdosol.axegestor.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    private String usuarioEmail;

    private String usuarioNome;

    @Column(nullable = false, length = 60)
    private String modulo;

    @Column(nullable = false, length = 60)
    private String acao;

    @Column(nullable = false, length = 80)
    private String entidade;

    private Long entidadeId;

    @Column(nullable = false, length = 255)
    private String descricao;
}
