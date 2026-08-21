package com.ilefilhosdosol.axegestor.service;

import com.ilefilhosdosol.axegestor.dto.AtualizarUsuarioRequest;
import com.ilefilhosdosol.axegestor.dto.RegistrarUsuarioRequest;
import com.ilefilhosdosol.axegestor.dto.UsuarioResponse;
import com.ilefilhosdosol.axegestor.enums.PerfilUsuario;
import com.ilefilhosdosol.axegestor.exception.BusinessException;
import com.ilefilhosdosol.axegestor.exception.NotFoundException;
import com.ilefilhosdosol.axegestor.model.Usuario;
import com.ilefilhosdosol.axegestor.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuditoriaService auditoriaService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Transactional
    public UsuarioResponse cadastrar(RegistrarUsuarioRequest request) {
        validarEmailDisponivel(request.email(), null);

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .ativo(true)
                .build();

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        auditoriaService.registrar("USUARIOS", "CADASTRAR", "Usuario", usuarioSalvo.getId(), "Cadastrou o usuario " + usuarioSalvo.getEmail());
        return UsuarioResponse.from(usuarioSalvo);
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, AtualizarUsuarioRequest request) {
        Usuario usuario = buscarUsuario(id);
        validarEmailDisponivel(request.email(), id);
        validarMudancaAdministrativa(usuario, request);

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setPerfil(request.perfil());
        usuario.setAtivo(request.ativo());

        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        auditoriaService.registrar("USUARIOS", "ATUALIZAR", "Usuario", usuarioSalvo.getId(), "Atualizou o usuario " + usuarioSalvo.getEmail());
        return UsuarioResponse.from(usuarioSalvo);
    }

    @Transactional
    public void inativar(Long id) {
        Usuario usuario = buscarUsuario(id);

        if (usuarioLogadoEh(usuario)) {
            throw new BusinessException("Voce nao pode inativar o proprio usuario");
        }

        if (ultimoAdminAtivo(usuario)) {
            throw new BusinessException("Nao e possivel inativar o ultimo ADMIN ativo");
        }

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
        auditoriaService.registrar("USUARIOS", "INATIVAR", "Usuario", usuario.getId(), "Inativou o usuario " + usuario.getEmail());
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));
    }

    private void validarEmailDisponivel(String email, Long usuarioIdAtual) {
        usuarioRepository.findByEmail(email)
                .filter(usuario -> usuarioIdAtual == null || !usuario.getId().equals(usuarioIdAtual))
                .ifPresent(usuario -> {
                    throw new BusinessException("E-mail ja cadastrado");
                });
    }

    private void validarMudancaAdministrativa(Usuario usuario, AtualizarUsuarioRequest request) {
        if (!PerfilUsuario.ADMIN.equals(usuario.getPerfil())) {
            return;
        }

        boolean deixaraDeSerAdminAtivo = !request.ativo() || !PerfilUsuario.ADMIN.equals(request.perfil());

        if (deixaraDeSerAdminAtivo && ultimoAdminAtivo(usuario)) {
            throw new BusinessException("Nao e possivel remover o ultimo ADMIN ativo");
        }
    }

    private boolean ultimoAdminAtivo(Usuario usuario) {
        return PerfilUsuario.ADMIN.equals(usuario.getPerfil())
                && Boolean.TRUE.equals(usuario.getAtivo())
                && usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.ADMIN) <= 1;
    }

    private boolean usuarioLogadoEh(Usuario usuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getPrincipal() instanceof Usuario usuarioLogado
                && usuarioLogado.getId().equals(usuario.getId());
    }
}
