package com.ilefilhosdosol.axegestor.controller;

import com.ilefilhosdosol.axegestor.dto.MaterialRequest;
import com.ilefilhosdosol.axegestor.dto.MaterialResponse;
import com.ilefilhosdosol.axegestor.dto.MovimentacaoEstoqueRequest;
import com.ilefilhosdosol.axegestor.dto.MovimentacaoEstoqueResponse;
import com.ilefilhosdosol.axegestor.enums.CategoriaMaterial;
import com.ilefilhosdosol.axegestor.service.EstoqueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/almoxarifado")
@PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUE')")
public class AlmoxarifadoController {

    private final EstoqueService estoqueService;

    public AlmoxarifadoController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @PostMapping("/materiais")
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialResponse cadastrarMaterial(@RequestBody @Valid MaterialRequest material) {
        return estoqueService.cadastrarMaterial(material);
    }

    @GetMapping("/materiais")
    public List<MaterialResponse> listarMateriais() {
        return estoqueService.listarMateriais();
    }

    @GetMapping("/materiais/buscar")
    public List<MaterialResponse> buscarPorNome(@RequestParam String nome) {
        return estoqueService.buscarPorNome(nome);
    }

    @GetMapping("/materiais/categoria")
    public List<MaterialResponse> buscarPorCategoria(@RequestParam CategoriaMaterial categoria) {
        return estoqueService.buscarPorCategoria(categoria);
    }

    @GetMapping("/materiais/estoque-baixo")
    public List<MaterialResponse> estoqueBaixo() {
        return estoqueService.estoqueBaixo();
    }

    @PostMapping("/movimentacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoEstoqueResponse registrarMovimentacao(@RequestBody @Valid MovimentacaoEstoqueRequest movimentacao) {
        return estoqueService.registrarMovimentacao(movimentacao);
    }

    @GetMapping("/movimentacoes/material/{materialId}")
    public List<MovimentacaoEstoqueResponse> listarMovimentacoesPorMaterial(@PathVariable Long materialId) {
        return estoqueService.listarMovimentacoesPorMaterial(materialId);
    }
}
