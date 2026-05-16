package com.ilefilhosdosol.axegestor.controller;

import com.ilefilhosdosol.axegestor.enums.StatusAssistencia;
import com.ilefilhosdosol.axegestor.exception.NotFoundException;
import com.ilefilhosdosol.axegestor.model.Assistencia;
import com.ilefilhosdosol.axegestor.model.SessaoTratamento;
import com.ilefilhosdosol.axegestor.repository.AssistenciaRepository;
import com.ilefilhosdosol.axegestor.repository.SessaoTratamentoRepository;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/assistencias")
@PreAuthorize("hasAnyRole('ADMIN', 'ASSISTENCIA')")
public class AssistenciaController {

    private final AssistenciaRepository assistenciaRepository;
    private final SessaoTratamentoRepository sessaoTratamentoRepository;
    private final AuditoriaService auditoriaService;

    public AssistenciaController(
            AssistenciaRepository assistenciaRepository,
            SessaoTratamentoRepository sessaoTratamentoRepository,
            AuditoriaService auditoriaService
    ) {
        this.assistenciaRepository = assistenciaRepository;
        this.sessaoTratamentoRepository = sessaoTratamentoRepository;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Assistencia cadastrar(@RequestBody @Valid Assistencia assistencia) {
        vincularSessoes(assistencia);
        Assistencia assistenciaSalva = assistenciaRepository.save(assistencia);
        auditoriaService.registrar("ASSISTENCIAS", "CADASTRAR", "Assistencia", assistenciaSalva.getId(), "Cadastrou assistência " + assistenciaSalva.getNome());
        return assistenciaSalva;
    }

    @GetMapping
    public List<Assistencia> listar() {
        return assistenciaRepository.findAll();
    }

    @GetMapping("/buscar/nome")
    public List<Assistencia> buscarPorNome(@RequestParam String nome) {
        return assistenciaRepository.findByNomeContainingIgnoreCase(nome);
    }

    @GetMapping("/buscar/whatsapp")
    public List<Assistencia> buscarPorWhatsapp(@RequestParam String whatsapp) {
        return assistenciaRepository.findByWhatsappContaining(whatsapp);
    }

    @GetMapping("/buscar/cidade")
    public List<Assistencia> buscarPorCidade(@RequestParam String cidade) {
        return assistenciaRepository.findByCidadeContainingIgnoreCase(cidade);
    }

    @GetMapping("/buscar/entidade")
    public List<Assistencia> buscarPorEntidade(@RequestParam String entidade) {
        return assistenciaRepository.findByEntidadeConsultaContainingIgnoreCase(entidade);
    }

    @GetMapping("/buscar/status")
    public List<Assistencia> buscarPorStatus(@RequestParam StatusAssistencia status) {
        return assistenciaRepository.findByStatus(status);
    }

    @GetMapping("/{id}")
    public Assistencia buscarPorId(@PathVariable Long id) {
        return buscarAssistencia(id);
    }

    @PutMapping("/{id}")
    public Assistencia atualizar(@PathVariable Long id, @RequestBody @Valid Assistencia assistenciaAtualizada) {
        Assistencia assistencia = buscarAssistencia(id);

        assistencia.setNome(assistenciaAtualizada.getNome());
        assistencia.setEntidadeConsulta(assistenciaAtualizada.getEntidadeConsulta());
        assistencia.setDataConsulta(assistenciaAtualizada.getDataConsulta());
        assistencia.setCidade(assistenciaAtualizada.getCidade());
        assistencia.setWhatsapp(assistenciaAtualizada.getWhatsapp());
        assistencia.setObservacoes(assistenciaAtualizada.getObservacoes());
        assistencia.setStatus(assistenciaAtualizada.getStatus());
        assistencia.setTratamentos(assistenciaAtualizada.getTratamentos());
        assistencia.setSessoes(assistenciaAtualizada.getSessoes());
        vincularSessoes(assistencia);

        Assistencia assistenciaSalva = assistenciaRepository.save(assistencia);
        auditoriaService.registrar("ASSISTENCIAS", "ATUALIZAR", "Assistencia", assistenciaSalva.getId(), "Atualizou assistência " + assistenciaSalva.getNome());
        return assistenciaSalva;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        if (!assistenciaRepository.existsById(id)) {
            throw new NotFoundException("Assistência não encontrada");
        }

        assistenciaRepository.deleteById(id);
        auditoriaService.registrar("ASSISTENCIAS", "DELETAR", "Assistencia", id, "Deletou a assistência de id " + id);
    }

    @PutMapping("/{idAssistencia}/sessoes/{idSessao}")
    public SessaoTratamento atualizarSessao(
            @PathVariable Long idAssistencia,
            @PathVariable Long idSessao,
            @RequestBody @Valid SessaoTratamento sessaoAtualizada
    ) {
        Assistencia assistencia = buscarAssistencia(idAssistencia);
        SessaoTratamento sessao = buscarSessao(idSessao);

        sessao.setAssistencia(assistencia);
        sessao.setNumeroSessao(sessaoAtualizada.getNumeroSessao());
        sessao.setDataSessao(sessaoAtualizada.getDataSessao());
        sessao.setRealizada(sessaoAtualizada.getRealizada());
        sessao.setObservacoes(sessaoAtualizada.getObservacoes());

        SessaoTratamento sessaoSalva = sessaoTratamentoRepository.save(sessao);
        auditoriaService.registrar("ASSISTENCIAS", "ATUALIZAR_SESSAO", "SessaoTratamento", sessaoSalva.getId(), "Atualizou sessão da assistência " + assistencia.getNome());
        return sessaoSalva;
    }

    @PutMapping("/{idAssistencia}/sessoes/{idSessao}/realizar")
    public SessaoTratamento marcarSessaoComoRealizada(
            @PathVariable Long idAssistencia,
            @PathVariable Long idSessao
    ) {
        Assistencia assistencia = buscarAssistencia(idAssistencia);
        SessaoTratamento sessao = buscarSessao(idSessao);

        sessao.setAssistencia(assistencia);
        sessao.setRealizada(true);

        SessaoTratamento sessaoSalva = sessaoTratamentoRepository.save(sessao);
        auditoriaService.registrar("ASSISTENCIAS", "REALIZAR_SESSAO", "SessaoTratamento", sessaoSalva.getId(), "Marcou sessão como realizada para " + assistencia.getNome());
        return sessaoSalva;
    }

    @PutMapping("/{id}/finalizar")
    public Assistencia finalizarTratamento(@PathVariable Long id) {
        Assistencia assistencia = buscarAssistencia(id);

        assistencia.setStatus(StatusAssistencia.FINALIZADO);

        Assistencia assistenciaSalva = assistenciaRepository.save(assistencia);
        auditoriaService.registrar("ASSISTENCIAS", "FINALIZAR", "Assistencia", assistenciaSalva.getId(), "Finalizou assistência " + assistenciaSalva.getNome());
        return assistenciaSalva;
    }

    @GetMapping("/historico")
    public List<Assistencia> historicoPorPessoa(@RequestParam String nome) {
        return assistenciaRepository.findByNomeContainingIgnoreCase(nome);
    }

    private Assistencia buscarAssistencia(Long id) {
        return assistenciaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assistência não encontrada"));
    }

    private SessaoTratamento buscarSessao(Long id) {
        return sessaoTratamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sessão não encontrada"));
    }

    private void vincularSessoes(Assistencia assistencia) {
        if (assistencia.getSessoes() != null) {
            assistencia.getSessoes().forEach(sessao -> sessao.setAssistencia(assistencia));
        }
    }
}
