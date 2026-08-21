package com.ilefilhosdosol.axegestor.controller;

import com.ilefilhosdosol.axegestor.dto.LoginRequest;
import com.ilefilhosdosol.axegestor.dto.LoginResponse;
import com.ilefilhosdosol.axegestor.dto.RegistrarUsuarioRequest;
import com.ilefilhosdosol.axegestor.dto.UsuarioResponse;
import com.ilefilhosdosol.axegestor.exception.BusinessException;
import com.ilefilhosdosol.axegestor.model.Usuario;
import com.ilefilhosdosol.axegestor.repository.UsuarioRepository;
import com.ilefilhosdosol.axegestor.service.TokenService;
import com.ilefilhosdosol.axegestor.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;

    public AuthController(
            UsuarioRepository usuarioRepository,
            TokenService tokenService,
            PasswordEncoder passwordEncoder,
            UsuarioService usuarioService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse registrar(@RequestBody @Valid RegistrarUsuarioRequest request) {
        return usuarioService.cadastrar(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("E-mail ou senha invalidos"));

        if (!usuario.isEnabled()) {
            throw new BusinessException("Usuario inativo");
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new BusinessException("E-mail ou senha invalidos");
        }

        String token = tokenService.gerarToken(usuario);

        return new LoginResponse(token);
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal Usuario usuario) {
        return UsuarioResponse.from(usuario);
    }
}
