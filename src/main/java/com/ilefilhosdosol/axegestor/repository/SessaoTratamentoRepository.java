package com.ilefilhosdosol.axegestor.repository;

import com.ilefilhosdosol.axegestor.model.SessaoTratamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessaoTratamentoRepository extends JpaRepository<SessaoTratamento, Long> {

    List<SessaoTratamento> findByAssistenciaId(Long assistenciaId);
}