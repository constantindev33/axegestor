package com.ilefilhosdosol.axegestor.config;

import com.ilefilhosdosol.axegestor.enums.PerfilUsuario;
import com.ilefilhosdosol.axegestor.model.Usuario;
import com.ilefilhosdosol.axegestor.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrapConfig {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapConfig.class);

    @Bean
    CommandLineRunner bootstrapAdmin(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.email:}") String adminEmail,
            @Value("${app.bootstrap-admin.password:}") String adminPassword,
            @Value("${app.bootstrap-admin.name:Administrador}") String adminName
    ) {
        return args -> {
            if (usuarioRepository.count() > 0) {
                return;
            }

            if (adminEmail.isBlank() || adminPassword.isBlank()) {
                log.warn("Nenhum usuario encontrado. Configure ADMIN_EMAIL e ADMIN_PASSWORD para criar o primeiro ADMIN automaticamente.");
                return;
            }

            if (adminPassword.length() < 8 || adminPassword.length() > 72) {
                throw new IllegalStateException("ADMIN_PASSWORD deve ter entre 8 e 72 caracteres.");
            }

            Usuario admin = Usuario.builder()
                    .nome(adminName)
                    .email(adminEmail)
                    .senha(passwordEncoder.encode(adminPassword))
                    .perfil(PerfilUsuario.ADMIN)
                    .ativo(true)
                    .build();

            usuarioRepository.save(admin);
            log.info("Primeiro usuario ADMIN criado automaticamente: {}", adminEmail);
        };
    }
}
