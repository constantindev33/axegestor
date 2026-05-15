package com.ilefilhosdosol.axegestor.repository;

import com.ilefilhosdosol.axegestor.enums.StatusAssistencia;
import com.ilefilhosdosol.axegestor.model.Assistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssistenciaRepository extends JpaRepository<Assistencia, Long> {

    List<Assistencia> findByNomeContainingIgnoreCase(String nome);

    List<Assistencia> findByWhatsappContaining(String whatsapp);

    List<Assistencia> findByCidadeContainingIgnoreCase(String cidade);

    List<Assistencia> findByEntidadeConsultaContainingIgnoreCase(String entidadeConsulta);

    List<Assistencia> findByStatus(StatusAssistencia status);
}