package com.ilefilhosdosol.axegestor.service;

import com.ilefilhosdosol.axegestor.dto.AtualizarUsuarioRequest;
import com.ilefilhosdosol.axegestor.enums.PerfilUsuario;
import com.ilefilhosdosol.axegestor.exception.BusinessException;
import com.ilefilhosdosol.axegestor.model.Usuario;
import com.ilefilhosdosol.axegestor.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private UsuarioService usuarioService;

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void naoDeveInativarUltimoAdminAtivo() {
        Usuario admin = Usuario.builder()
                .id(1L)
                .nome("Administrador")
                .email("admin@axegestor.local")
                .senha("hash")
                .perfil(PerfilUsuario.ADMIN)
                .ativo(true)
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> usuarioService.inativar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Nao e possivel inativar o ultimo ADMIN ativo");

        verify(usuarioRepository, never()).save(admin);
    }

    @Test
    void naoDevePermitirQueUsuarioInativeASiMesmo() {
        Usuario admin = Usuario.builder()
                .id(1L)
                .nome("Administrador")
                .email("admin@axegestor.local")
                .senha("hash")
                .perfil(PerfilUsuario.ADMIN)
                .ativo(true)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities())
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> usuarioService.inativar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Voce nao pode inativar o proprio usuario");

        verify(usuarioRepository, never()).save(admin);
    }

    @Test
    void naoDeveRemoverPerfilDoUltimoAdminAtivo() {
        Usuario admin = Usuario.builder()
                .id(1L)
                .nome("Administrador")
                .email("admin@axegestor.local")
                .senha("hash")
                .perfil(PerfilUsuario.ADMIN)
                .ativo(true)
                .build();

        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
                "Administrador",
                "admin@axegestor.local",
                null,
                PerfilUsuario.FINANCEIRO,
                true
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findByEmail("admin@axegestor.local")).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> usuarioService.atualizar(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Nao e possivel remover o ultimo ADMIN ativo");

        verify(usuarioRepository, never()).save(admin);
    }
}
