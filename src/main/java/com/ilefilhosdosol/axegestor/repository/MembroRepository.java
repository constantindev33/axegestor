package com.ilefilhosdosol.axegestor.repository;

import com.ilefilhosdosol.axegestor.model.Membro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembroRepository extends JpaRepository<Membro, Long> {
}