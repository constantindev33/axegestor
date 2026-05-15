package com.ilefilhosdosol.axegestor.repository;

import com.ilefilhosdosol.axegestor.enums.CategoriaMaterial;
import com.ilefilhosdosol.axegestor.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByNomeContainingIgnoreCase(String nome);

    List<Material> findByCategoria(CategoriaMaterial categoria);

    @Query("select material from Material material where material.quantidadeAtual <= material.quantidadeMinima")
    List<Material> findByEstoqueBaixo();
}
