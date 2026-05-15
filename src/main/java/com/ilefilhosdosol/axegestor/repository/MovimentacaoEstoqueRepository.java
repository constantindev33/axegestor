package com.ilefilhosdosol.axegestor.repository;

import com.ilefilhosdosol.axegestor.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByMaterialId(Long materialId);
}