package com.ilefilhosdosol.axegestor.service;

import com.ilefilhosdosol.axegestor.enums.StatusAssistencia;
import com.ilefilhosdosol.axegestor.exception.BusinessException;
import com.ilefilhosdosol.axegestor.exception.NotFoundException;
import com.ilefilhosdosol.axegestor.model.Assistencia;
import com.ilefilhosdosol.axegestor.model.SessaoTratamento;
import com.ilefilhosdosol.axegestor.repository.AssistenciaRepository;
import com.ilefilhosdosol.axegestor.repository.SessaoTratamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssistenciaService {

    private final AssistenciaRepository assistenciaRepository;
    private final SessaoTratamentoRepository sessaoTratamentoRepository;
    private final AuditoriaService auditoriaService;

    public AssistenciaService(
            AssistenciaRepository assistenciaRepository,
            SessaoTratamentoRepository sessaoTratamentoRepository,
            AuditoriaService auditoriaService
    ) {
        this.assistenciaRepository = assistenciaRepository;
        this.sessaoTratamentoRepository = sessaoTratamentoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Assistencia cadastrar(Assistencia assistencia) {
        vincularSessoes(assistencia);
        Assistencia assistenciaSalva = assistenciaRepository.save(assistencia);
        auditoriaService.registrar("ASSISTENCIAS", "CADASTRAR", "Assistencia", assistenciaSalva.getId(), "Cadastrou assistencia " + assistenciaSalva.getNome());
        return assistenciaSalva;
    }

    public List<Assistencia> listar() {
        return assistenciaRepository.findAll();
    }

    public List<Assistencia> buscarPorNome(String nome) {
        return assistenciaRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Assistencia> buscarPorWhatsapp(String whatsapp) {
        return assistenciaRepository.findByWhatsappContaining(whatsapp);
    }

    public List<Assistencia> buscarPorCidade(String cidade) {
        return assistenciaRepository.findByCidadeContainingIgnoreCase(cidade);
    }

    public List<Assistencia> buscarPorEntidade(String entidade) {
        return assistenciaRepository.findByEntidadeConsultaContainingIgnoreCase(entidade);
    }

    public List<Assistencia> buscarPorStatus(StatusAssistencia status) {
        return assistenciaRepository.findByStatus(status);
    }

    public Assistencia buscarPorId(Long id) {
        return buscarAssistencia(id);
    }

    @Transactional
    public Assistencia atualizar(Long id, Assistencia assistenciaAtualizada) {
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
        auditoriaService.registrar("ASSISTENCIAS", "ATUALIZAR", "Assistencia", assistenciaSalva.getId(), "Atualizou assistencia " + assistenciaSalva.getNome());
        return assistenciaSalva;
    }

    @Transactional
    public void deletar(Long id) {
        if (!assistenciaRepository.existsById(id)) {
            throw new NotFoundException("Assistencia nao encontrada");
        }

        assistenciaRepository.deleteById(id);
        auditoriaService.registrar("ASSISTENCIAS", "DELETAR", "Assistencia", id, "Deletou a assistencia de id " + id);
    }

    @Transactional
    public SessaoTratamento atualizarSessao(Long idAssistencia, Long idSessao, SessaoTratamento sessaoAtualizada) {
        Assistencia assistencia = buscarAssistencia(idAssistencia);
        SessaoTratamento sessao = buscarSessaoDaAssistencia(idAssistencia, idSessao);

        sessao.setAssistencia(assistencia);
        sessao.setNumeroSessao(sessaoAtualizada.getNumeroSessao());
        sessao.setDataSessao(sessaoAtualizada.getDataSessao());
        sessao.setRealizada(sessaoAtualizada.getRealizada());
        sessao.setObservacoes(sessaoAtualizada.getObservacoes());

        SessaoTratamento sessaoSalva = sessaoTratamentoRepository.save(sessao);
        auditoriaService.registrar("ASSISTENCIAS", "ATUALIZAR_SESSAO", "SessaoTratamento", sessaoSalva.getId(), "Atualizou sessao da assistencia " + assistencia.getNome());
        return sessaoSalva;
    }

    @Transactional
    public SessaoTratamento marcarSessaoComoRealizada(Long idAssistencia, Long idSessao) {
        Assistencia assistencia = buscarAssistencia(idAssistencia);
        SessaoTratamento sessao = buscarSessaoDaAssistencia(idAssistencia, idSessao);

        sessao.setAssistencia(assistencia);
        sessao.setRealizada(true);

        SessaoTratamento sessaoSalva = sessaoTratamentoRepository.save(sessao);
        auditoriaService.registrar("ASSISTENCIAS", "REALIZAR_SESSAO", "SessaoTratamento", sessaoSalva.getId(), "Marcou sessao como realizada para " + assistencia.getNome());
        return sessaoSalva;
    }

    @Transactional
    public Assistencia finalizarTratamento(Long id) {
        Assistencia assistencia = buscarAssistencia(id);
        assistencia.setStatus(StatusAssistencia.FINALIZADO);

        Assistencia assistenciaSalva = assistenciaRepository.save(assistencia);
        auditoriaService.registrar("ASSISTENCIAS", "FINALIZAR", "Assistencia", assistenciaSalva.getId(), "Finalizou assistencia " + assistenciaSalva.getNome());
        return assistenciaSalva;
    }

    public List<Assistencia> historicoPorPessoa(String nome) {
        return assistenciaRepository.findByNomeContainingIgnoreCase(nome);
    }

    private Assistencia buscarAssistencia(Long id) {
        return assistenciaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assistencia nao encontrada"));
    }

    private SessaoTratamento buscarSessaoDaAssistencia(Long idAssistencia, Long idSessao) {
        SessaoTratamento sessao = sessaoTratamentoRepository.findById(idSessao)
                .orElseThrow(() -> new NotFoundException("Sessao nao encontrada"));

        if (sessao.getAssistencia() == null || !idAssistencia.equals(sessao.getAssistencia().getId())) {
            throw new BusinessException("Sessao nao pertence a assistencia informada");
        }

        return sessao;
    }

    private void vincularSessoes(Assistencia assistencia) {
        if (assistencia.getSessoes() != null) {
            assistencia.getSessoes().forEach(sessao -> sessao.setAssistencia(assistencia));
        }
    }
}
