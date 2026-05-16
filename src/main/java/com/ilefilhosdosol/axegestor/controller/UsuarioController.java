package com.ilefilhosdosol.axegestor.controller;

import com.ilefilhosdosol.axegestor.dto.AtualizarUsuarioRequest;
import com.ilefilhosdosol.axegestor.dto.RegistrarUsuarioRequest;
import com.ilefilhosdosol.axegestor.dto.UsuarioResponse;
import com.ilefilhosdosol.axegestor.exception.BusinessException;
import com.ilefilhosdosol.axegestor.exception.NotFoundException;
import com.ilefilhosdosol.axegestor.model.Usuario;
import com.ilefilhosdosol.axegestor.repository.UsuarioRepository;
import com.ilefilhosdosol.axegestor.service.AuditoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuditoriaService auditoriaService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@RequestBody @Valid RegistrarUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("E-mail já cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .ativo(true)
                .build();

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        auditoriaService.registrar("USUARIOS", "CADASTRAR", "Usuario", usuarioSalvo.getId(), "Cadastrou o usuário " + usuarioSalvo.getEmail());
        return UsuarioResponse.from(usuarioSalvo);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarUsuarioRequest request
    ) {
        Usuario usuario = buscarUsuario(id);

        usuarioRepository.findByEmail(request.email())
                .filter(usuarioEncontrado -> !usuarioEncontrado.getId().equals(id))
                .ifPresent(usuarioEncontrado -> {
                    throw new BusinessException("E-mail já cadastrado");
                });

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setPerfil(request.perfil());
        usuario.setAtivo(request.ativo());

        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        auditoriaService.registrar("USUARIOS", "ATUALIZAR", "Usuario", usuarioSalvo.getId(), "Atualizou o usuário " + usuarioSalvo.getEmail());
        return UsuarioResponse.from(usuarioSalvo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable Long id) {
        Usuario usuario = buscarUsuario(id);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
        auditoriaService.registrar("USUARIOS", "INATIVAR", "Usuario", usuario.getId(), "Inativou o usuário " + usuario.getEmail());
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }
}
