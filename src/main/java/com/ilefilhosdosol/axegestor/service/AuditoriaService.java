package com.ilefilhosdosol.axegestor.service;

import com.ilefilhosdosol.axegestor.model.Auditoria;
import com.ilefilhosdosol.axegestor.model.Usuario;
import com.ilefilhosdosol.axegestor.repository.AuditoriaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public void registrar(
            String modulo,
            String acao,
            String entidade,
            Long entidadeId,
            String descricao
    ) {
        Usuario usuario = usuarioAtual();

        Auditoria auditoria = Auditoria.builder()
                .criadoEm(LocalDateTime.now())
                .usuarioEmail(usuario != null ? usuario.getEmail() : "sistema")
                .usuarioNome(usuario != null ? usuario.getNome() : "Sistema")
                .modulo(modulo)
                .acao(acao)
                .entidade(entidade)
                .entidadeId(entidadeId)
                .descricao(descricao)
                .build();

        auditoriaRepository.save(auditoria);
    }

    private Usuario usuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            return null;
        }

        return usuario;
    }
}
