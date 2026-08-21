package com.ilefilhosdosol.axegestor.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilefilhosdosol.axegestor.dto.LoginRequest;
import com.ilefilhosdosol.axegestor.enums.PerfilUsuario;
import com.ilefilhosdosol.axegestor.model.Usuario;
import com.ilefilhosdosol.axegestor.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparBanco() {
        usuarioRepository.deleteAll();
    }

    @Test
    void loginComCredenciaisValidasRetornaJwtEPermiteConsultarUsuarioLogado() throws Exception {
        criarUsuario("Admin Teste", "admin@teste.local", "senha-segura", PerfilUsuario.ADMIN, true);

        String token = login("admin@teste.local", "senha-segura");

        assertThat(token).isNotBlank();

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Admin Teste"))
                .andExpect(jsonPath("$.email").value("admin@teste.local"))
                .andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test
    void loginComSenhaInvalidaRetornaBadRequestSemToken() throws Exception {
        criarUsuario("Admin Teste", "admin@teste.local", "senha-correta", PerfilUsuario.ADMIN, true);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@teste.local", "senha-errada"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("E-mail ou senha invalidos"));
    }

    @Test
    void loginComUsuarioInativoRetornaBadRequest() throws Exception {
        criarUsuario("Usuario Inativo", "inativo@teste.local", "senha-segura", PerfilUsuario.ADMIN, false);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("inativo@teste.local", "senha-segura"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Usuario inativo"));
    }

    @Test
    void rotaProtegidaSemTokenNaoPermiteAcesso() throws Exception {
        mockMvc.perform(get("/membros"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPodeAcessarTelaDeUsuarios() throws Exception {
        criarUsuario("Admin Teste", "admin@teste.local", "senha-segura", PerfilUsuario.ADMIN, true);
        String token = login("admin@teste.local", "senha-segura");

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void financeiroNaoPodeAcessarTelaDeUsuarios() throws Exception {
        criarUsuario("Financeiro Teste", "financeiro@teste.local", "senha-segura", PerfilUsuario.FINANCEIRO, true);
        String token = login("financeiro@teste.local", "senha-segura");

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void financeiroPodeAcessarModuloFinanceiro() throws Exception {
        criarUsuario("Financeiro Teste", "financeiro@teste.local", "senha-segura", PerfilUsuario.FINANCEIRO, true);
        String token = login("financeiro@teste.local", "senha-segura");

        mockMvc.perform(get("/financeiro")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void assistenciaNaoPodeAcessarModuloFinanceiro() throws Exception {
        criarUsuario("Assistencia Teste", "assistencia@teste.local", "senha-segura", PerfilUsuario.ASSISTENCIA, true);
        String token = login("assistencia@teste.local", "senha-segura");

        mockMvc.perform(get("/financeiro")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void estoquePodeAcessarModuloMembrosMasNaoModuloAssistencias() throws Exception {
        criarUsuario("Estoque Teste", "estoque@teste.local", "senha-segura", PerfilUsuario.ESTOQUE, true);
        String token = login("estoque@teste.local", "senha-segura");

        mockMvc.perform(get("/membros")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/assistencias")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String login(String email, String senha) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, senha))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    private Usuario criarUsuario(
            String nome,
            String email,
            String senha,
            PerfilUsuario perfil,
            boolean ativo
    ) {
        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .perfil(perfil)
                .ativo(ativo)
                .build();

        return usuarioRepository.save(usuario);
    }
}
