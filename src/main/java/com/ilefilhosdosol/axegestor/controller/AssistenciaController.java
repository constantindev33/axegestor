package com.ilefilhosdosol.axegestor.controller;

import com.ilefilhosdosol.axegestor.dto.AssistenciaRequest;
import com.ilefilhosdosol.axegestor.dto.AssistenciaResponse;
import com.ilefilhosdosol.axegestor.dto.SessaoTratamentoRequest;
import com.ilefilhosdosol.axegestor.dto.SessaoTratamentoResponse;
import com.ilefilhosdosol.axegestor.enums.StatusAssistencia;
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
    public AssistenciaResponse cadastrar(@RequestBody @Valid AssistenciaRequest assistencia) {
        return assistenciaService.cadastrar(assistencia);
    }

    @GetMapping
    public List<AssistenciaResponse> listar() {
        return assistenciaService.listar();
    }

    @GetMapping("/buscar/nome")
    public List<AssistenciaResponse> buscarPorNome(@RequestParam String nome) {
        return assistenciaService.buscarPorNome(nome);
    }

    @GetMapping("/buscar/whatsapp")
    public List<AssistenciaResponse> buscarPorWhatsapp(@RequestParam String whatsapp) {
        return assistenciaService.buscarPorWhatsapp(whatsapp);
    }

    @GetMapping("/buscar/cidade")
    public List<AssistenciaResponse> buscarPorCidade(@RequestParam String cidade) {
        return assistenciaService.buscarPorCidade(cidade);
    }

    @GetMapping("/buscar/entidade")
    public List<AssistenciaResponse> buscarPorEntidade(@RequestParam String entidade) {
        return assistenciaService.buscarPorEntidade(entidade);
    }

    @GetMapping("/buscar/status")
    public List<AssistenciaResponse> buscarPorStatus(@RequestParam StatusAssistencia status) {
        return assistenciaService.buscarPorStatus(status);
    }

    @GetMapping("/{id}")
    public AssistenciaResponse buscarPorId(@PathVariable Long id) {
        return assistenciaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public AssistenciaResponse atualizar(@PathVariable Long id, @RequestBody @Valid AssistenciaRequest assistenciaAtualizada) {
        return assistenciaService.atualizar(id, assistenciaAtualizada);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        assistenciaService.deletar(id);
    }

    @PutMapping("/{idAssistencia}/sessoes/{idSessao}")
    public SessaoTratamentoResponse atualizarSessao(
            @PathVariable Long idAssistencia,
            @PathVariable Long idSessao,
            @RequestBody @Valid SessaoTratamentoRequest sessaoAtualizada
    ) {
        return assistenciaService.atualizarSessao(idAssistencia, idSessao, sessaoAtualizada);
    }

    @PutMapping("/{idAssistencia}/sessoes/{idSessao}/realizar")
    public SessaoTratamentoResponse marcarSessaoComoRealizada(
            @PathVariable Long idAssistencia,
            @PathVariable Long idSessao
    ) {
        return assistenciaService.marcarSessaoComoRealizada(idAssistencia, idSessao);
    }

    @PutMapping("/{id}/finalizar")
    public AssistenciaResponse finalizarTratamento(@PathVariable Long id) {
        return assistenciaService.finalizarTratamento(id);
    }

    @GetMapping("/historico")
    public List<AssistenciaResponse> historicoPorPessoa(@RequestParam String nome) {
        return assistenciaService.historicoPorPessoa(nome);
    }
}
