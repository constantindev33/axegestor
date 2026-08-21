package com.ilefilhosdosol.axegestor.controller;

import com.ilefilhosdosol.axegestor.enums.StatusAssistencia;
import com.ilefilhosdosol.axegestor.model.Assistencia;
import com.ilefilhosdosol.axegestor.model.SessaoTratamento;
import com.ilefilhosdosol.axegestor.service.AssistenciaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/assistencias")
@PreAuthorize("hasAnyRole('ADMIN', 'ASSISTENCIA')")
public class AssistenciaController {

    private final AssistenciaService assistenciaService;

    public AssistenciaController(AssistenciaService assistenciaService) {
        this.assistenciaService = assistenciaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Assistencia cadastrar(@RequestBody @Valid Assistencia assistencia) {
        return assistenciaService.cadastrar(assistencia);
    }

    @GetMapping
    public List<Assistencia> listar() {
        return assistenciaService.listar();
    }

    @GetMapping("/buscar/nome")
    public List<Assistencia> buscarPorNome(@RequestParam String nome) {
        return assistenciaService.buscarPorNome(nome);
    }

    @GetMapping("/buscar/whatsapp")
    public List<Assistencia> buscarPorWhatsapp(@RequestParam String whatsapp) {
        return assistenciaService.buscarPorWhatsapp(whatsapp);
    }

    @GetMapping("/buscar/cidade")
    public List<Assistencia> buscarPorCidade(@RequestParam String cidade) {
        return assistenciaService.buscarPorCidade(cidade);
    }

    @GetMapping("/buscar/entidade")
    public List<Assistencia> buscarPorEntidade(@RequestParam String entidade) {
        return assistenciaService.buscarPorEntidade(entidade);
    }

    @GetMapping("/buscar/status")
    public List<Assistencia> buscarPorStatus(@RequestParam StatusAssistencia status) {
        return assistenciaService.buscarPorStatus(status);
    }

    @GetMapping("/{id}")
    public Assistencia buscarPorId(@PathVariable Long id) {
        return assistenciaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Assistencia atualizar(@PathVariable Long id, @RequestBody @Valid Assistencia assistenciaAtualizada) {
        return assistenciaService.atualizar(id, assistenciaAtualizada);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        assistenciaService.deletar(id);
    }

    @PutMapping("/{idAssistencia}/sessoes/{idSessao}")
    public SessaoTratamento atualizarSessao(
            @PathVariable Long idAssistencia,
            @PathVariable Long idSessao,
            @RequestBody @Valid SessaoTratamento sessaoAtualizada
    ) {
        return assistenciaService.atualizarSessao(idAssistencia, idSessao, sessaoAtualizada);
    }

    @PutMapping("/{idAssistencia}/sessoes/{idSessao}/realizar")
    public SessaoTratamento marcarSessaoComoRealizada(
            @PathVariable Long idAssistencia,
            @PathVariable Long idSessao
    ) {
        return assistenciaService.marcarSessaoComoRealizada(idAssistencia, idSessao);
    }

    @PutMapping("/{id}/finalizar")
    public Assistencia finalizarTratamento(@PathVariable Long id) {
        return assistenciaService.finalizarTratamento(id);
    }

    @GetMapping("/historico")
    public List<Assistencia> historicoPorPessoa(@RequestParam String nome) {
        return assistenciaService.historicoPorPessoa(nome);
    }
}
