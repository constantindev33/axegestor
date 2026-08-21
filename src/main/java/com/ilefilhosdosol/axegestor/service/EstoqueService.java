package com.ilefilhosdosol.axegestor.service;

import com.ilefilhosdosol.axegestor.enums.CategoriaMaterial;
import com.ilefilhosdosol.axegestor.enums.TipoMovimentacaoEstoque;
import com.ilefilhosdosol.axegestor.exception.BusinessException;
import com.ilefilhosdosol.axegestor.exception.NotFoundException;
import com.ilefilhosdosol.axegestor.model.Material;
import com.ilefilhosdosol.axegestor.model.MovimentacaoEstoque;
import com.ilefilhosdosol.axegestor.repository.MaterialRepository;
import com.ilefilhosdosol.axegestor.repository.MovimentacaoEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EstoqueService {

    private final MaterialRepository materialRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final AuditoriaService auditoriaService;

    public EstoqueService(
            MaterialRepository materialRepository,
            MovimentacaoEstoqueRepository movimentacaoRepository,
            AuditoriaService auditoriaService
    ) {
        this.materialRepository = materialRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Material cadastrarMaterial(Material material) {
        Material materialSalvo = materialRepository.save(material);
        auditoriaService.registrar("ESTOQUE", "CADASTRAR", "Material", materialSalvo.getId(), "Cadastrou material " + materialSalvo.getNome());
        return materialSalvo;
    }

    public List<Material> listarMateriais() {
        return materialRepository.findAll();
    }

    public List<Material> buscarPorNome(String nome) {
        return materialRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Material> buscarPorCategoria(CategoriaMaterial categoria) {
        return materialRepository.findByCategoria(categoria);
    }

    public List<Material> estoqueBaixo() {
        return materialRepository.findByEstoqueBaixo();
    }

    @Transactional
    public MovimentacaoEstoque registrarMovimentacao(MovimentacaoEstoque movimentacao) {
        Long materialId = extrairMaterialId(movimentacao);
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new NotFoundException("Material nao encontrado"));

        atualizarQuantidadeMaterial(material, movimentacao);
        materialRepository.save(material);

        movimentacao.setMaterial(material);
        movimentacao.setDataMovimentacao(LocalDate.now());

        MovimentacaoEstoque movimentacaoSalva = movimentacaoRepository.save(movimentacao);
        auditoriaService.registrar("ESTOQUE", "MOVIMENTAR", "MovimentacaoEstoque", movimentacaoSalva.getId(), "Registrou " + movimentacaoSalva.getTipo() + " de " + material.getNome());
        return movimentacaoSalva;
    }

    public List<MovimentacaoEstoque> listarMovimentacoesPorMaterial(Long materialId) {
        if (!materialRepository.existsById(materialId)) {
            throw new NotFoundException("Material nao encontrado");
        }

        return movimentacaoRepository.findByMaterialId(materialId);
    }

    private Long extrairMaterialId(MovimentacaoEstoque movimentacao) {
        if (movimentacao.getMaterial() == null || movimentacao.getMaterial().getId() == null) {
            throw new BusinessException("Informe o id do material");
        }

        return movimentacao.getMaterial().getId();
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
            throw new BusinessException("Estoque insuficiente para realizar a saida");
        }

        material.setQuantidadeAtual(novaQuantidade);
    }
}
