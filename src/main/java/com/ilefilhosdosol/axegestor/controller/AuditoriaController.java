package com.ilefilhosdosol.axegestor.controller;

import com.ilefilhosdosol.axegestor.model.Auditoria;
import com.ilefilhosdosol.axegestor.repository.AuditoriaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auditorias")
@PreAuthorize("hasRole('ADMIN')")
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaController(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @GetMapping
    public List<Auditoria> listar(@RequestParam(required = false) String modulo) {
        if (modulo != null && !modulo.isBlank()) {
            return auditoriaRepository.findTop100ByModuloOrderByCriadoEmDesc(modulo);
        }

        return auditoriaRepository.findTop100ByOrderByCriadoEmDesc();
    }
}
