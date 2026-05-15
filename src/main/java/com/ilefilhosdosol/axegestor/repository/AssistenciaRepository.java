package com.ilefilhosdosol.axegestor.repository;

import com.ilefilhosdosol.axegestor.enums.StatusAssistencia;
import com.ilefilhosdosol.axegestor.model.Assistencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssistenciaRepository extends JpaRepository<Assistencia, Long> {

    @Override
    @EntityGraph(attributePaths = {"tratamentos", "sessoes"})
    List<Assistencia> findAll();

    @EntityGraph(attributePaths = {"tratamentos", "sessoes"})
    List<Assistencia> findByNomeContainingIgnoreCase(String nome);

    @EntityGraph(attributePaths = {"tratamentos", "sessoes"})
    List<Assistencia> findByWhatsappContaining(String whatsapp);

    @EntityGraph(attributePaths = {"tratamentos", "sessoes"})
    List<Assistencia> findByCidadeContainingIgnoreCase(String cidade);

    @EntityGraph(attributePaths = {"tratamentos", "sessoes"})
    List<Assistencia> findByEntidadeConsultaContainingIgnoreCase(String entidadeConsulta);

    @EntityGraph(attributePaths = {"tratamentos", "sessoes"})
    List<Assistencia> findByStatus(StatusAssistencia status);
}
