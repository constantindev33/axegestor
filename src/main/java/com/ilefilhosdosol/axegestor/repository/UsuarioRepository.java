package com.ilefilhosdosol.axegestor.repository;

import com.ilefilhosdosol.axegestor.enums.PerfilUsuario;
import com.ilefilhosdosol.axegestor.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByPerfilAndAtivoTrue(PerfilUsuario perfil);
}
