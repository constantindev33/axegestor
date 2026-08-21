package com.ilefilhosdosol.axegestor.service;

import com.ilefilhosdosol.axegestor.dto.AssistenciaRequest;
import com.ilefilhosdosol.axegestor.dto.AssistenciaResponse;
import com.ilefilhosdosol.axegestor.dto.SessaoTratamentoRequest;
import com.ilefilhosdosol.axegestor.dto.SessaoTratamentoResponse;
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
    public AssistenciaResponse cadastrar(AssistenciaRequest request) {
        Assistencia assistencia = montarAssistencia(request);
        vincularSessoes(assistencia);
        Assistencia assistenciaSalva = assistenciaRepository.save(assistencia);
        auditoriaService.registrar("ASSISTENCIAS", "CADASTRAR", "Assistencia", assistenciaSalva.getId(), "Cadastrou assistencia " + assistenciaSalva.getNome());
        return AssistenciaResponse.from(assistenciaSalva);
    }

    public List<AssistenciaResponse> listar() {
        return mapearAssistencias(assistenciaRepository.findAll());
    }

    public List<AssistenciaResponse> buscarPorNome(String nome) {
        return mapearAssistencias(assistenciaRepository.findByNomeContainingIgnoreCase(nome));
    }

    public List<AssistenciaResponse> buscarPorWhatsapp(String whatsapp) {
        return mapearAssistencias(assistenciaRepository.findByWhatsappContaining(whatsapp));
    }

    public List<AssistenciaResponse> buscarPorCidade(String cidade) {
        return mapearAssistencias(assistenciaRepository.findByCidadeContainingIgnoreCase(cidade));
    }

    public List<AssistenciaResponse> buscarPorEntidade(String entidade) {
        return mapearAssistencias(assistenciaRepository.findByEntidadeConsultaContainingIgnoreCase(entidade));
    }

    public List<AssistenciaResponse> buscarPorStatus(StatusAssistencia status) {
        return mapearAssistencias(assistenciaRepository.findByStatus(status));
    }

    public AssistenciaResponse buscarPorId(Long id) {
        return AssistenciaResponse.from(buscarAssistencia(id));
    }

    @Transactional
    public AssistenciaResponse atualizar(Long id, AssistenciaRequest request) {
        Assistencia assistencia = buscarAssistencia(id);

        aplicarDados(assistencia, request);
        vincularSessoes(assistencia);

        Assistencia assistenciaSalva = assistenciaRepository.save(assistencia);
        auditoriaService.registrar("ASSISTENCIAS", "ATUALIZAR", "Assistencia", assistenciaSalva.getId(), "Atualizou assistencia " + assistenciaSalva.getNome());
        return AssistenciaResponse.from(assistenciaSalva);
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
    public SessaoTratamentoResponse atualizarSessao(Long idAssistencia, Long idSessao, SessaoTratamentoRequest sessaoAtualizada) {
        Assistencia assistencia = buscarAssistencia(idAssistencia);
        SessaoTratamento sessao = buscarSessaoDaAssistencia(idAssistencia, idSessao);

        sessao.setAssistencia(assistencia);
        sessao.setNumeroSessao(sessaoAtualizada.numeroSessao());
        sessao.setDataSessao(sessaoAtualizada.dataSessao());
        sessao.setRealizada(sessaoAtualizada.realizada());
        sessao.setObservacoes(sessaoAtualizada.observacoes());

        SessaoTratamento sessaoSalva = sessaoTratamentoRepository.save(sessao);
        auditoriaService.registrar("ASSISTENCIAS", "ATUALIZAR_SESSAO", "SessaoTratamento", sessaoSalva.getId(), "Atualizou sessao da assistencia " + assistencia.getNome());
        return SessaoTratamentoResponse.from(sessaoSalva);
    }

    @Transactional
    public SessaoTratamentoResponse marcarSessaoComoRealizada(Long idAssistencia, Long idSessao) {
        Assistencia assistencia = buscarAssistencia(idAssistencia);
        SessaoTratamento sessao = buscarSessaoDaAssistencia(idAssistencia, idSessao);

        sessao.setAssistencia(assistencia);
        sessao.setRealizada(true);

        SessaoTratamento sessaoSalva = sessaoTratamentoRepository.save(sessao);
        auditoriaService.registrar("ASSISTENCIAS", "REALIZAR_SESSAO", "SessaoTratamento", sessaoSalva.getId(), "Marcou sessao como realizada para " + assistencia.getNome());
        return SessaoTratamentoResponse.from(sessaoSalva);
    }

    @Transactional
    public AssistenciaResponse finalizarTratamento(Long id) {
        Assistencia assistencia = buscarAssistencia(id);
        assistencia.setStatus(StatusAssistencia.FINALIZADO);

        Assistencia assistenciaSalva = assistenciaRepository.save(assistencia);
        auditoriaService.registrar("ASSISTENCIAS", "FINALIZAR", "Assistencia", assistenciaSalva.getId(), "Finalizou assistencia " + assistenciaSalva.getNome());
        return AssistenciaResponse.from(assistenciaSalva);
    }

    public List<AssistenciaResponse> historicoPorPessoa(String nome) {
        return mapearAssistencias(assistenciaRepository.findByNomeContainingIgnoreCase(nome));
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

    private Assistencia montarAssistencia(AssistenciaRequest request) {
        Assistencia assistencia = new Assistencia();
        aplicarDados(assistencia, request);
        return assistencia;
    }

    private void aplicarDados(Assistencia assistencia, AssistenciaRequest request) {
        assistencia.setNome(request.nome());
        assistencia.setEntidadeConsulta(request.entidadeConsulta());
        assistencia.setDataConsulta(request.dataConsulta());
        assistencia.setCidade(request.cidade());
        assistencia.setWhatsapp(request.whatsapp());
        assistencia.setObservacoes(request.observacoes());
        assistencia.setStatus(request.status());
        assistencia.setTratamentos(request.tratamentos());
        assistencia.setSessoes(montarSessoes(request));
    }

    private List<SessaoTratamento> montarSessoes(AssistenciaRequest request) {
        if (request.sessoes() == null) {
            return List.of();
        }

        return request.sessoes().stream()
                .map(sessao -> SessaoTratamento.builder()
                        .numeroSessao(sessao.numeroSessao())
                        .dataSessao(sessao.dataSessao())
                        .realizada(sessao.realizada())
                        .observacoes(sessao.observacoes())
                        .build())
                .toList();
    }

    private void vincularSessoes(Assistencia assistencia) {
        if (assistencia.getSessoes() != null) {
            assistencia.getSessoes().forEach(sessao -> sessao.setAssistencia(assistencia));
        }
    }

    private List<AssistenciaResponse> mapearAssistencias(List<Assistencia> assistencias) {
        return assistencias.stream()
                .map(AssistenciaResponse::from)
                .toList();
    }
}
