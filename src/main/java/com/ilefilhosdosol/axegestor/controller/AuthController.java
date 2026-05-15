package com.ilefilhosdosol.axegestor.controller;

import com.ilefilhosdosol.axegestor.dto.LoginRequest;
import com.ilefilhosdosol.axegestor.dto.LoginResponse;
import com.ilefilhosdosol.axegestor.dto.RegistrarUsuarioRequest;
import com.ilefilhosdosol.axegestor.dto.UsuarioResponse;
import com.ilefilhosdosol.axegestor.exception.BusinessException;
import com.ilefilhosdosol.axegestor.model.Usuario;
import com.ilefilhosdosol.axegestor.repository.UsuarioRepository;
import com.ilefilhosdosol.axegestor.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    public AuthController(
            UsuarioRepository usuarioRepository,
            TokenService tokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@RequestBody @Valid RegistrarUsuarioRequest request) {
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

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos"));

        if (!usuario.isEnabled()) {
            throw new BusinessException("Usuário inativo");
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new BusinessException("E-mail ou senha inválidos");
        }

        String token = tokenService.gerarToken(usuario);

        return new LoginResponse(token);
    }
}
