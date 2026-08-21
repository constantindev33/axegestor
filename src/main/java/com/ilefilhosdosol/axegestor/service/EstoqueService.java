package com.ilefilhosdosol.axegestor.service;

import com.ilefilhosdosol.axegestor.dto.MaterialRequest;
import com.ilefilhosdosol.axegestor.dto.MaterialResponse;
import com.ilefilhosdosol.axegestor.dto.MovimentacaoEstoqueRequest;
import com.ilefilhosdosol.axegestor.dto.MovimentacaoEstoqueResponse;
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
    public MaterialResponse cadastrarMaterial(MaterialRequest request) {
        Material material = montarMaterial(request);
        Material materialSalvo = materialRepository.save(material);
        auditoriaService.registrar("ESTOQUE", "CADASTRAR", "Material", materialSalvo.getId(), "Cadastrou material " + materialSalvo.getNome());
        return MaterialResponse.from(materialSalvo);
    }

    public List<MaterialResponse> listarMateriais() {
        return mapearMateriais(materialRepository.findAll());
    }

    public List<MaterialResponse> buscarPorNome(String nome) {
        return mapearMateriais(materialRepository.findByNomeContainingIgnoreCase(nome));
    }

    public List<MaterialResponse> buscarPorCategoria(CategoriaMaterial categoria) {
        return mapearMateriais(materialRepository.findByCategoria(categoria));
    }

    public List<MaterialResponse> estoqueBaixo() {
        return mapearMateriais(materialRepository.findByEstoqueBaixo());
    }

    @Transactional
    public MovimentacaoEstoqueResponse registrarMovimentacao(MovimentacaoEstoqueRequest request) {
        Long materialId = extrairMaterialId(request);
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new NotFoundException("Material nao encontrado"));

        MovimentacaoEstoque movimentacao = montarMovimentacao(request);
        atualizarQuantidadeMaterial(material, movimentacao);
        materialRepository.save(material);

        movimentacao.setMaterial(material);
        movimentacao.setDataMovimentacao(LocalDate.now());

        MovimentacaoEstoque movimentacaoSalva = movimentacaoRepository.save(movimentacao);
        auditoriaService.registrar("ESTOQUE", "MOVIMENTAR", "MovimentacaoEstoque", movimentacaoSalva.getId(), "Registrou " + movimentacaoSalva.getTipo() + " de " + material.getNome());
        return MovimentacaoEstoqueResponse.from(movimentacaoSalva);
    }

    public List<MovimentacaoEstoqueResponse> listarMovimentacoesPorMaterial(Long materialId) {
        if (!materialRepository.existsById(materialId)) {
            throw new NotFoundException("Material nao encontrado");
        }

        return movimentacaoRepository.findByMaterialId(materialId).stream()
                .map(MovimentacaoEstoqueResponse::from)
                .toList();
    }

    private Material montarMaterial(MaterialRequest request) {
        return Material.builder()
                .nome(request.nome())
                .categoria(request.categoria())
                .quantidadeAtual(request.quantidadeAtual())
                .quantidadeMinima(request.quantidadeMinima())
                .unidadeMedida(request.unidadeMedida())
                .localArmazenamento(request.localArmazenamento())
                .observacoes(request.observacoes())
                .build();
    }

    private MovimentacaoEstoque montarMovimentacao(MovimentacaoEstoqueRequest request) {
        return MovimentacaoEstoque.builder()
                .tipo(request.tipo())
                .quantidade(request.quantidade())
                .responsavel(request.responsavel())
                .motivo(request.motivo())
                .observacoes(request.observacoes())
                .build();
    }

    private Long extrairMaterialId(MovimentacaoEstoqueRequest request) {
        if (request.material() == null || request.material().id() == null) {
            throw new BusinessException("Informe o id do material");
        }

        return request.material().id();
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

    private List<MaterialResponse> mapearMateriais(List<Material> materiais) {
        return materiais.stream()
                .map(MaterialResponse::from)
                .toList();
    }
}
