package com.ilefilhosdosol.axegestor.model;

import com.ilefilhosdosol.axegestor.enums.FuncaoMembro;
import com.ilefilhosdosol.axegestor.enums.StatusMembro;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "membros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    private String nome;

    @Size(max = 30, message = "Telefone deve ter no máximo 30 caracteres")
    private String telefone;

    @Email(message = "E-mail deve ser válido")
    @Size(max = 160, message = "E-mail deve ter no máximo 160 caracteres")
    private String email;

    private LocalDate dataEntrada;

    @NotNull(message = "Função é obrigatória")
    @Enumerated(EnumType.STRING)
    private FuncaoMembro funcao;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    private StatusMembro status;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}
