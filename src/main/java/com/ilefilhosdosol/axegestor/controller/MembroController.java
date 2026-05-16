package com.ilefilhosdosol.axegestor.controller;

import com.ilefilhosdosol.axegestor.exception.NotFoundException;
import com.ilefilhosdosol.axegestor.model.Membro;
import com.ilefilhosdosol.axegestor.repository.MembroRepository;
import com.ilefilhosdosol.axegestor.service.AuditoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/membros")
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO', 'ASSISTENCIA', 'ESTOQUE')")
public class MembroController {

    private final MembroRepository membroRepository;
    private final AuditoriaService auditoriaService;

    public MembroController(
            MembroRepository membroRepository,
            AuditoriaService auditoriaService
    ) {
        this.membroRepository = membroRepository;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Membro cadastrar(@RequestBody @Valid Membro membro) {
        Membro membroSalvo = membroRepository.save(membro);
        auditoriaService.registrar("MEMBROS", "CADASTRAR", "Membro", membroSalvo.getId(), "Cadastrou o membro " + membroSalvo.getNome());
        return membroSalvo;
    }

    @GetMapping
    public List<Membro> listar() {
        return membroRepository.findAll();
    }

    @GetMapping("/{id}")
    public Membro buscarPorId(@PathVariable Long id) {
        return buscarMembro(id);
    }

    @PutMapping("/{id}")
    public Membro atualizar(@PathVariable Long id, @RequestBody @Valid Membro membroAtualizado) {
        Membro membro = buscarMembro(id);

        membro.setNome(membroAtualizado.getNome());
        membro.setTelefone(membroAtualizado.getTelefone());
        membro.setEmail(membroAtualizado.getEmail());
        membro.setDataEntrada(membroAtualizado.getDataEntrada());
        membro.setFuncao(membroAtualizado.getFuncao());
        membro.setStatus(membroAtualizado.getStatus());
        membro.setObservacoes(membroAtualizado.getObservacoes());

        Membro membroSalvo = membroRepository.save(membro);
        auditoriaService.registrar("MEMBROS", "ATUALIZAR", "Membro", membroSalvo.getId(), "Atualizou o membro " + membroSalvo.getNome());
        return membroSalvo;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        if (!membroRepository.existsById(id)) {
            throw new NotFoundException("Membro não encontrado");
        }

        membroRepository.deleteById(id);
        auditoriaService.registrar("MEMBROS", "DELETAR", "Membro", id, "Deletou o membro de id " + id);
    }

    private Membro buscarMembro(Long id) {
        return membroRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Membro não encontrado"));
    }
}
