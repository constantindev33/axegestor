package com.ilefilhosdosol.axegestor.repository;

import com.ilefilhosdosol.axegestor.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findTop100ByOrderByCriadoEmDesc();

    List<Auditoria> findTop100ByModuloOrderByCriadoEmDesc(String modulo);
}
