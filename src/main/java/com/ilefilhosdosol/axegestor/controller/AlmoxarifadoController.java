package com.ilefilhosdosol.axegestor.controller;

import com.ilefilhosdosol.axegestor.enums.CategoriaMaterial;
import com.ilefilhosdosol.axegestor.enums.TipoMovimentacaoEstoque;
import com.ilefilhosdosol.axegestor.exception.BusinessException;
import com.ilefilhosdosol.axegestor.exception.NotFoundException;
import com.ilefilhosdosol.axegestor.model.Material;
import com.ilefilhosdosol.axegestor.model.MovimentacaoEstoque;
import com.ilefilhosdosol.axegestor.repository.MaterialRepository;
import com.ilefilhosdosol.axegestor.repository.MovimentacaoEstoqueRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/almoxarifado")
public class AlmoxarifadoController {

    private final MaterialRepository materialRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    public AlmoxarifadoController(
            MaterialRepository materialRepository,
            MovimentacaoEstoqueRepository movimentacaoRepository
    ) {
        this.materialRepository = materialRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    @PostMapping("/materiais")
    @ResponseStatus(HttpStatus.CREATED)
    public Material cadastrarMaterial(@RequestBody @Valid Material material) {
        return materialRepository.save(material);
    }

    @GetMapping("/materiais")
    public List<Material> listarMateriais() {
        return materialRepository.findAll();
    }

    @GetMapping("/materiais/buscar")
    public List<Material> buscarPorNome(@RequestParam String nome) {
        return materialRepository.findByNomeContainingIgnoreCase(nome);
    }

    @GetMapping("/materiais/categoria")
    public List<Material> buscarPorCategoria(@RequestParam CategoriaMaterial categoria) {
        return materialRepository.findByCategoria(categoria);
    }

    @GetMapping("/materiais/estoque-baixo")
    public List<Material> estoqueBaixo() {
        return materialRepository.findByEstoqueBaixo();
    }

    @PostMapping("/movimentacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoEstoque registrarMovimentacao(
            @RequestBody @Valid MovimentacaoEstoque movimentacao
    ) {
        if (movimentacao.getMaterial().getId() == null) {
            throw new BusinessException("Informe o id do material");
        }

        Material material = materialRepository.findById(movimentacao.getMaterial().getId())
                .orElseThrow(() -> new NotFoundException("Material não encontrado"));

        atualizarQuantidadeMaterial(material, movimentacao);
        materialRepository.save(material);

        movimentacao.setMaterial(material);
        movimentacao.setDataMovimentacao(LocalDate.now());

        return movimentacaoRepository.save(movimentacao);
    }

    @GetMapping("/movimentacoes/material/{materialId}")
    public List<MovimentacaoEstoque> listarMovimentacoesPorMaterial(
            @PathVariable Long materialId
    ) {
        if (!materialRepository.existsById(materialId)) {
            throw new NotFoundException("Material não encontrado");
        }

        return movimentacaoRepository.findByMaterialId(materialId);
    }

    private void atualizarQuantidadeMaterial(Material material, MovimentacaoEstoque movimentacao) {
        int quantidadeAtual = material.getQuantidadeAtual();
        int quantidadeMovimentada = movimentacao.getQuantidade();

        if (movimentacao.getTipo() == TipoMovimentacaoEstoque.ENTRADA) {
            material.setQuantidadeAtual(quantidadeAtual + quantidadeMovimentada);
            return;
        }

        int novaQuantidade = quantidadeAtual - quantidadeMovimentada;

        if (novaQuantidade < 0) {
            throw new BusinessException("Estoque insuficiente para realizar a saída");
        }

        material.setQuantidadeAtual(novaQuantidade);
    }
}
